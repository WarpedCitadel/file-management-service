package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.filemanager.model.FileMetaDataModel;
import com.warpedcitadel.fileuploadservice.filemanager.model.ImageMetaDataModel;
import com.warpedcitadel.fileuploadservice.validation.FileValidation;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

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


    public void uploadFileToS3(MultipartFile file, FileMetaDataModel fileDetails) throws IOException {
        String fileUUID = recordFileMetaData(file, fileDetails);

        if (fileUUID.isEmpty()) throw new BadRequestException("Incorrect File Type.");

        String prefix = "users/" + fileDetails.getAppUserUuid() + "/games/" + fileDetails.getGameProfileUUID() + "/" + fileUUID;

        uploadFileS3(file, prefix);
    }


    public void uploadImageToS3(MultipartFile file, ImageMetaDataModel imageDetails) throws IOException {
        String imageUUID = recordImageMetaData(file, imageDetails);

        if (imageUUID.isEmpty()) throw new BadRequestException("Incorrect File Type.");

        String prefix = "users/" + imageDetails.getAppUserUUID() + "/images/" + imageUUID;

        uploadFileS3(file, prefix);
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
