package com.warpedcitadel.fileuploadservice.filemanager;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class FileService {

    @Autowired
    private final S3Client s3Client;

    @Autowired private FileRepository repository;

    private FileModel fileModel;

    @Value("${aws.bucket.name}")
    private String bucketName;


    @Autowired
    public FileService(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    // Todo | Expand function to include metadata extraction
    public void uploadFile(MultipartFile file) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        String objectURL = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName)
                .key(file.getOriginalFilename())).toExternalForm();




        FileModel fileMetadata = new FileModel(1,
                file.getOriginalFilename(),
                objectURL,
                "1",
                "13KB",
                file.getContentType(),
                1);

//        fileMetadata(fileMetadata);
    }

    public int fileMetadata(FileModel file){
        return repository.recordFile(file);
    }


    public byte[] downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        return objectAsBytes.asByteArray();
    }
}
