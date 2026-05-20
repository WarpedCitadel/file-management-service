package com.warpedcitadel.fileuploadservice.filemanager;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

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


    public void uploadFileToS3(MultipartFile file, FileMetaDataModel fileDetails){

       String fileUUID = recordFileMetaData(file, fileDetails);

        try {
            uploadFileS3(file, fileUUID);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to upload file", exception);
        }
    }

    private String recordFileMetaData(MultipartFile file, FileMetaDataModel fileDetails){
        long appUserid = repository.getUserByUuid(fileDetails.getAppUserUuid());
        String fileSize = getBytesToString(file);


        FileMetaDataModel metaData = new FileMetaDataModel(
                appUserid,
                fileDetails.getFileName(),
                fileDetails.getFileVersion(),
                fileSize
        );
        return repository.recordFileMetaData(metaData);
    }


    private void uploadFileS3(MultipartFile file, String fileUUID) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileUUID)
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        String objectURL = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName)
                .key(fileUUID)).toExternalForm();

    }


//    ### HELPER FUNCTIONS ###

    private String getBytesToString(MultipartFile file){
        long sizeInBytes = file.getSize();
        String convertedBytes = DataSize.ofBytes(sizeInBytes).toString();
        return convertedBytes;
    }
}
