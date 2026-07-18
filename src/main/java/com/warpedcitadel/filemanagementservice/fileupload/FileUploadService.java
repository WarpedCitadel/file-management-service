package com.warpedcitadel.filemanagementservice.fileupload;


import com.warpedcitadel.filemanagementservice.fileupload.dto.FileUploadDto;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDetails;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDto;
import com.warpedcitadel.filemanagementservice.fileupload.model.FileMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.validation.FileValidation;
import com.warpedcitadel.filemanagementservice.util.VirusScanService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${aws.valid-bucket.name}")
    private String bucketName;

    private final S3Client s3Client;
    private final FileUploadRepository repository;
    private final FileValidation fileValidation;
    private final VirusScanService clamAVClient;


    public FileUploadService(S3Client s3Client, FileUploadRepository repository,
                             FileValidation fileValidation, VirusScanService clamAVClient) {

        this.s3Client = s3Client;
        this.repository = repository;
        this.fileValidation = fileValidation;
        this.clamAVClient = clamAVClient;
    }


    public void uploadFileToS3(MultipartFile file, FileUploadDto fileUploadDto) throws IOException {
        recordFileMetaData(file, fileUploadDto);

        String prefix = "games/" + fileUploadDto.gameProfileUUID() + "/files/" + file.getOriginalFilename();

        uploadFileS3(file, prefix);
    }


    public void uploadImageToS3(MultipartFile file, ImageMetaDataModel imageDetails) throws IOException {

        ImageMetaDataModel image = recordImageMetaData(file, imageDetails);

        String prefix = "images/users/" + image.getFileUUID() + "/image/" + image.getFileName();
        uploadFileS3(file, prefix);
        boolean result = clamAVClient.processFile(file);

        if (result) {
            System.out.println("File ready for transfer");
        }
    }


    public void uploadGameImageToS3(List<MultipartFile> files, List<GameImageDetails> fileDetails) throws IOException {

        if (files.size() != fileDetails.size()) {
            throw new IllegalArgumentException(
                    "Each file must have corresponding fileDetails.");
        }

        List<GameImageDto> imageMetaData = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {

            imageMetaData.add(new GameImageDto(files.get(i), fileDetails.get(i)));
        }

        for (int i = 0; i < imageMetaData.size(); i++) {

            if (!fileValidation.isValidFile(imageMetaData.get(i).file(),
                    new String[]{".jpeg", ".png", ".jpg"}, 2000000)) {

                throw new IllegalArgumentException("Invalid File type: " + imageMetaData.get(i).file().getContentType());
            }
        }

        List<String> fileNames = recordGameImageMetaData(imageMetaData);

        for (int i = 0; files.size() > i; i++) {

            String prefix = "images/games/" + fileDetails.get(i).gameProfileUUID() + "/gameImages/" + fileNames.get(i);

            uploadFileS3(files.get(i), prefix);
        }

    }


    private String recordFileMetaData(MultipartFile file, FileUploadDto fileUploadDto) {

        if (!fileValidation.isValidFile(file, new String[]{".zip"}, 1000000000)) return "";

        String fileSize = formatBytes(file);

        FileMetaDataModel metaData = new FileMetaDataModel(
                fileUploadDto.gameProfileUUID(),
                file.getOriginalFilename(),
                fileUploadDto.fileVersion(),
                fileUploadDto.platformOS(),
                fileSize
        );

        return repository.recordFileMetaData(metaData);
    }


    public ImageMetaDataModel recordImageMetaData(MultipartFile file, ImageMetaDataModel imageDetails) {
        if (!fileValidation.isValidFile(file, new String[]{".jpeg", ".png", ".jpg"}, 2000000)) {

            throw new IllegalArgumentException("Wrong file format: " + file.getOriginalFilename());
        };
        String fileSize = formatBytes(file);

        ImageMetaDataModel imageMetaData = new ImageMetaDataModel(
                imageDetails.getAppUserUUID(),
                updateFileName(file.getOriginalFilename()),
                fileSize
        );

        return repository.recordImageMetaData(imageMetaData);
    }


    public List<String> recordGameImageMetaData(List<GameImageDto> gameImageList) {

        List<ImageMetaDataModel> gameImageModelList = new ArrayList<>();

        for (int i = 0; gameImageList.size() > i; i++) {

            String fileSize = formatBytes(gameImageList.get(i).file());

            ImageMetaDataModel imageMetaDataModel = new ImageMetaDataModel(
                    gameImageList.get(i).details().gameProfileUUID(),
                    updateFileName(gameImageList.get(i).file().getOriginalFilename()),
                    fileSize,
                    gameImageList.get(i).details().isCover()
            );

            gameImageModelList.add(imageMetaDataModel);
        }

        return repository.recordGameImageMetaData(gameImageModelList);
    }


    private void uploadFileS3(MultipartFile file, String key) throws IOException {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            s3Client.utilities().getUrl(builder -> builder.bucket(bucketName)
                    .key(key)).toExternalForm();
        } catch (IOException failedUploadException) {

            throw new IOException("Failed to upload file", failedUploadException);
        }
    }


//    ### HELPER FUNCTIONS ###
    private String formatBytes(MultipartFile file) {
        long sizeInBytes = file.getSize();

        if (sizeInBytes < 1024) {
            return sizeInBytes + "B";
        } else {
            sizeInBytes = sizeInBytes / 1024;
            if (sizeInBytes < 1024) {
                return sizeInBytes + "KB";
            }
            sizeInBytes = sizeInBytes / 1024;
            return sizeInBytes + "MB";
        }
    }


    // TODO: update logic later and possibly use data from DB
    private String updateFileName(String rawFileName){

        int lastDotIndex = rawFileName.lastIndexOf(".");

        if (lastDotIndex > 0 && lastDotIndex < rawFileName.length() - 1) {

            String newFilename = UUID.randomUUID().toString();
            return newFilename + rawFileName.substring(lastDotIndex);
        }

        return rawFileName;
    }
}

