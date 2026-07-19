package com.warpedcitadel.filemanagementservice.fileupload;


import com.warpedcitadel.filemanagementservice.filetransfer.FileTransferService;
import com.warpedcitadel.filemanagementservice.filetransfer.dto.RequestData;
import com.warpedcitadel.filemanagementservice.fileupload.dto.FileUploadDto;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDetails;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDto;
import com.warpedcitadel.filemanagementservice.fileupload.model.FileMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageFileTransferModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.validation.FileValidation;
import com.warpedcitadel.filemanagementservice.util.VirusScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${aws.valid-bucket.name}")
    private String validName;

    @Value("${aws.image-bucket.name}")
    private String imageBucketName;

    private final S3Client s3Client;
    private final FileUploadRepository repository;
    private final FileValidation fileValidation;
    private final VirusScanService clamAVClient;
    private final FileTransferService fileTransferService;
    private final Logger log = LoggerFactory.getLogger(FileUploadService.class);


    public FileUploadService(S3Client s3Client, FileUploadRepository repository,
                             FileValidation fileValidation, VirusScanService clamAVClient, FileTransferService fileTransferService) {

        this.s3Client = s3Client;
        this.repository = repository;
        this.fileValidation = fileValidation;
        this.clamAVClient = clamAVClient;
        this.fileTransferService = fileTransferService;
    }


    public void uploadFileToS3(MultipartFile file, FileUploadDto fileUploadDto) throws IOException {
        recordFileMetaData(file, fileUploadDto);

        String prefix = "games/" + fileUploadDto.gameProfileUUID() + "/files/" + file.getOriginalFilename();

        uploadFileS3(file, prefix);
    }


    public void uploadImageToS3(MultipartFile file, ImageMetaDataModel imageDetails) throws IOException {
        try {
            if (!fileValidation.isValidFile(file,
                    new String[]{".jpeg", ".png", ".jpg"}, 2000000)) {
                throw new IllegalArgumentException("Invalid file type: " + file.getContentType());
            }
            ImageMetaDataModel image = recordImageMetaDataToStaging(file, imageDetails);
            String prefix = "images/users/" + image.getFileUUID() + "/image/" + image.getFileName();
            uploadFileS3(file, prefix);
            boolean result = clamAVClient.processFile(file);
            if (!result) {
                deleteS3Objects(validName, prefix);
                repository.deleteStagingImage(imageDetails);
                throw new IOException("Malformed file content detected");
            } else {
                ImageFileTransferModel profileImageModel = repository.recordImageMetaData(imageDetails);
                RequestData profileImage = new RequestData(
                        profileImageModel.getNewFileUUID(),
                        profileImageModel.getNewFileName()
                );
                String oldPrefix = "images/users/" + profileImageModel.getOldFileUUID() +
                        "/image/" + profileImageModel.getOldFileName();
                fileTransferService.transferProfileImageToS3(profileImage);
                if (!isStaticImage(profileImageModel.getOldFileUUID())) {
                    deleteS3Objects(imageBucketName, oldPrefix);
                }
            }
        } catch (IOException exception) {
            log.error("Failed to upload image file: {}", file.getOriginalFilename());
            throw exception;
        }
    }


    public void uploadGameImageToS3(List<MultipartFile> files, List<GameImageDetails> fileDetails) throws IOException {
        try {
            if (files.size() != fileDetails.size()) {
                throw new IllegalArgumentException(
                        "Each file must have corresponding file details.");
            }
            List<GameImageDto> imageMetaData = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                if (!fileValidation.isValidFile(files.get(i),
                        new String[]{".jpeg", ".png", ".jpg"}, 2000000)) {
                    throw new IllegalArgumentException("Invalid file type: " + files.get(i).getContentType());
                }
                imageMetaData.add(new GameImageDto(files.get(i), fileDetails.get(i)));
            }
            List<String> fileNames = recordGameImageToStaging(imageMetaData);
            List<String> prefixList = new ArrayList<>();
            List<RequestData> gameImages = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                String prefix = "images/games/" + fileDetails.get(i).gameProfileUUID() + "/gameImages/" + fileNames.get(i);
                uploadFileS3(files.get(i), prefix);
                RequestData image = new RequestData(
                        fileDetails.get(i).gameProfileUUID(),
                        fileNames.get(i)
                );
                gameImages.add(image);
                prefixList.add(prefix);
                boolean result = clamAVClient.processFile(files.get(i));
                if (!result) {
                    for (int j = 0; prefixList.size() > j; j++) {
                        deleteS3Objects(validName, prefixList.get(j));
                    }
                    repository.deleteStagingGameImages(fileDetails.getFirst().gameProfileUUID());
                    throw new IOException("Malformed file detected");
                }
            }
            if (files.size() == gameImages.size()) {
                repository.recordGameImageMetaData(fileDetails.getFirst().gameProfileUUID());
                fileTransferService.transferGameImagesToS3(gameImages);
                log.info("A total of ({}) images files uploaded successfully for game profile ID: ({})",
                        gameImages.size() ,fileDetails.getFirst().gameProfileUUID());
            }
        } catch (IOException exception) {
            log.error("Failed to upload game images for game profile ID: {}", fileDetails.getFirst().gameProfileUUID());
            throw exception;
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


    public ImageMetaDataModel recordImageMetaDataToStaging(MultipartFile file, ImageMetaDataModel imageDetails) {
        if (!fileValidation.isValidFile(file, new String[]{".jpeg", ".png", ".jpg"}, 2000000)) {

            throw new IllegalArgumentException("Wrong file format: " + file.getOriginalFilename());
        };
        String fileSize = formatBytes(file);

        ImageMetaDataModel imageMetaData = new ImageMetaDataModel(
                imageDetails.getAppUserUUID(),
                updateFileName(file.getOriginalFilename()),
                fileSize
        );

        return repository.recordImageMetaDataToStaging(imageMetaData);
    }


    public List<String> recordGameImageToStaging(List<GameImageDto> gameImageList) {
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
        return repository.recordGameImageToStaging(gameImageModelList);
    }


    private void uploadFileS3(MultipartFile file, String key) throws IOException {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(validName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            s3Client.utilities().getUrl(builder -> builder.bucket(validName)
                    .key(key)).toExternalForm();
        } catch (IOException failedUploadException) {

            throw new IOException("Failed to upload file", failedUploadException);
        }
    }


//    ### HELPER FUNCTIONS ###
    private void deleteS3Objects(String bucketName, String prefix) {

        String continuationToken = null;
        do {

            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build();

            ListObjectsV2Response objectList = s3Client.listObjectsV2(listRequest);
            List<ObjectIdentifier> deletionList = new ArrayList<>();

            for (S3Object s3Object : objectList.contents()) {
                deletionList.add(ObjectIdentifier.builder().key(s3Object.key()).build());
            }

            if (!deletionList.isEmpty()) {
                DeleteObjectsRequest deleteS3Objects = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(builder -> builder.objects(deletionList))
                        .build();

                s3Client.deleteObjects(deleteS3Objects);
            }

            continuationToken = objectList.nextContinuationToken();
        } while (continuationToken != null);
    }


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


    private String updateFileName(String rawFileName){
        int lastDotIndex = rawFileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < rawFileName.length() - 1) {
            String newFilename = UUID.randomUUID().toString();
            return newFilename + rawFileName.substring(lastDotIndex);
        }
        return rawFileName;
    }


    private boolean isStaticImage(String fileUUID) {
        String[] staticAssets = new String[]{"019f7b16-635a-7c13-b15d-ed3c75ad61f9"};
        for (String file : staticAssets) {
            if (fileUUID.equals(file)) {
                return true;
            }
        }
        return false;
    }
}