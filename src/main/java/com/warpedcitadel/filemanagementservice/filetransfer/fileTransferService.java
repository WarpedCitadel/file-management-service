package com.warpedcitadel.filemanagementservice.filetransfer;

import com.warpedcitadel.filemanagementservice.filetransfer.dto.RequestData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class fileTransferService {

    @Value("${cloud.aws.region}")
    private String region;

    @Value("${aws.valid-bucket.name}")
    private String validBucketName;

    @Value("${aws.game-bucket.name}")
    private String gameBucketName;

    @Value("${aws.image-bucket.name}")
    private String imageBucketName;

    private final S3Client s3Client;

    public fileTransferService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // Logic should only be applied to HTML5 based games
    public void extractZip(RequestData requestData) {

        String prefix = "games/" + requestData.objectUUID() + "/files/" + requestData.fileName();

        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(
                GetObjectRequest
                        .builder()
                        .bucket(validBucketName)
                        .key(prefix)
                        .build()
        );

            try (ZipInputStream zip = new ZipInputStream(object)) {

                ZipEntry entry;

                while ((entry = zip.getNextEntry()) != null) {

                    if (entry.isDirectory()) {
                        continue;
                    }

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    zip.transferTo(byteArrayOutputStream);

                    PutObjectRequest put =
                            PutObjectRequest.builder()
                                    .bucket(gameBucketName)
                                    .key(prefix + "/" + entry.getName())
                                    .contentType(getContentType(entry.getName()))
                                    .build();

                    s3Client.putObject(
                            put,
                            RequestBody.fromBytes(byteArrayOutputStream.toByteArray())
                    );

                    zip.closeEntry();
            }

        } catch (IOException exception) {

            throw new RuntimeException("Failed to unzip file contents of "
                    + requestData.fileName()
                    + " to storage", exception);
        }
    }

    public void transferGameFileToS3(List<RequestData> fileData){

        for (int i = 0; fileData.size() > i; i++) {

            try {
                String prefix = "games/" + fileData.get(i).objectUUID() + "/files/" + fileData.get(i).fileName();

                CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                        .sourceBucket(validBucketName)
                        .sourceKey(prefix)
                        .destinationBucket(gameBucketName)
                        .destinationKey(prefix)
                        .build();

                s3Client.copyObject(copyRequest);
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(validBucketName)
                        .key(prefix)
                        .build();

                s3Client.deleteObject(deleteRequest);

            } catch (RuntimeException exception) {

                throw new RuntimeException("Failed to transfer file contents of "
                        + fileData.get(i).fileName()
                        + " to storage", exception);
            }
        }
    }

    public void transferGameImagesToS3(List<RequestData> imageData){

        for (int i = 0; imageData.size() > i; i++) {

            try {
                String prefix = "images/games/" + imageData.get(i).objectUUID() + "/gameImages/" + imageData.get(i).fileName();

                CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                        .sourceBucket(validBucketName)
                        .sourceKey(prefix)
                        .destinationBucket(imageBucketName)
                        .destinationKey(prefix)
                        .build();

                s3Client.copyObject(copyRequest);
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(validBucketName)
                        .key(prefix)
                        .build();

                s3Client.deleteObject(deleteRequest);

            } catch (RuntimeException exception) {

                throw new RuntimeException("Failed to transfer file contents of "
                        + imageData.get(i).fileName()
                        + " to storage", exception);
            }
        }
    }


    public void transferProfileImageToS3(RequestData imageData){

            try {
                String prefix = "images/users/" + imageData.objectUUID() + "/image/" + imageData.fileName();

                CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                        .sourceBucket(validBucketName)
                        .sourceKey(prefix)
                        .destinationBucket(imageBucketName)
                        .destinationKey(prefix)
                        .build();

                s3Client.copyObject(copyRequest);
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(validBucketName)
                        .key(prefix)
                        .build();

                s3Client.deleteObject(deleteRequest);

            } catch (RuntimeException exception) {

                throw new RuntimeException("Failed to transfer file contents of "
                        + imageData.fileName()
                        + " to storage", exception);
        }
    }


    // ## Helper functions ##
    private String getContentType(String filename) {

        return switch (filename.substring(filename.lastIndexOf('.') + 1)) {
            case "html" -> "text/html";
            case "js" -> "application/javascript";
            case "css" -> "text/css";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "json" -> "application/json";
            case "wasm" -> "application/wasm";
            default -> "application/octet-stream";
        };
    }
}