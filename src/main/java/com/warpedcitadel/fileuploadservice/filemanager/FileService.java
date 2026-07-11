package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.filemanager.dto.GameImageDetails;
import com.warpedcitadel.fileuploadservice.filemanager.dto.GameImageDto;
import com.warpedcitadel.fileuploadservice.filemanager.model.FileMetaDataModel;
import com.warpedcitadel.fileuploadservice.filemanager.model.ImageMetaDataModel;
import com.warpedcitadel.fileuploadservice.validation.FileValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    @Value("${aws.valid-bucket.name}")
    private String bucketName;

    @Autowired
    private final S3Client s3Client;

    @Autowired
    private FileMetaDataRepository repository;

    @Autowired
    private FileValidation fileValidation;


    @Autowired
    public FileService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // TODO: provide proper formatting and receive name from DB instead
    public void uploadFileToS3(MultipartFile file, FileMetaDataModel fileDetails) throws IOException {
        recordFileMetaData(file, fileDetails);

        String prefix = "games/" + fileDetails.getGameProfileUUID() + "/files/" + file.getOriginalFilename();

        uploadFileS3(file, prefix);
    }


    public void uploadImageToS3(MultipartFile file, ImageMetaDataModel imageDetails) throws IOException {
        recordImageMetaData(file, imageDetails);

        String prefix = "users/" + imageDetails.getAppUserUUID() + "/images/" + file.getOriginalFilename();

        uploadFileS3(file, prefix);
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

        recordGameImageMetaData(imageMetaData);

        for (int i = 0; files.size() > i; i++) {

            String prefix = "images/games/" + fileDetails.get(i).gameProfileUUID() + "/gameImages/" + files.get(i).getOriginalFilename();

            uploadFileS3(files.get(i), prefix);
        }

    }


    private String recordFileMetaData(MultipartFile file, FileMetaDataModel fileDetails) {

        if (!fileValidation.isValidFile(file, new String[]{".zip"}, 1000000000)) return "";

        String fileSize = formatBytes(file);

        FileMetaDataModel metaData = new FileMetaDataModel(
                fileDetails.getGameProfileUUID(),
                file.getOriginalFilename(),
                fileDetails.getFileVersion(),
                fileSize
        );

        return repository.recordFileMetaData(metaData);
    }


    public String recordImageMetaData(MultipartFile file, ImageMetaDataModel imageDetails) {
        if (!fileValidation.isValidFile(file, new String[]{".jpeg", ".png", ".jpg"}, 2000000)) return "";
        String fileSize = formatBytes(file);

        ImageMetaDataModel imageMetaData = new ImageMetaDataModel(
                imageDetails.getAppUserUUID(),
                file.getOriginalFilename(),
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
                    gameImageList.get(i).file().getOriginalFilename(),
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
}
