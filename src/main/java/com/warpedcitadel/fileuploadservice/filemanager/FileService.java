package com.warpedcitadel.fileuploadservice.filemanager;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.sql.SQLException;

@Service
public class FileService {

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Autowired
    private final S3Client s3Client;

    @Autowired
    private FileMetaDataRepository repository;


    @Autowired
    public FileService(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    public void uploadFileToS3(MultipartFile file, FileMetaDataModel fileDetails) throws SQLException, IOException {
        String fileUUID = recordFileMetaData(file, fileDetails);
        uploadFileS3(file, fileUUID);
    }

    private String recordFileMetaData(MultipartFile file, FileMetaDataModel fileDetails) throws SQLException {
        long appUserid = repository.getUserByUuid(fileDetails.getAppUserUuid());
        String fileSize = formatBytes(file);

        FileMetaDataModel metaData = new FileMetaDataModel(
                appUserid,
                file.getOriginalFilename(),
                fileDetails.getFileVersion(),
                fileSize
        );
        return repository.recordFileMetaData(metaData);
    }


    private void uploadFileS3(MultipartFile file, String fileUUID) throws IOException {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileUUID)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));

            String objectURL = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName)
                    .key(fileUUID)).toExternalForm();
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
