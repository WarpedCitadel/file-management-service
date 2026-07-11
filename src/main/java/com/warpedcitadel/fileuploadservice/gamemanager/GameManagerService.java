package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
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
public class GameManagerService {

    @Value("${cloud.aws.region}")
    private String region;

    @Value("${aws.valid-bucket.name}")
    private String validBucketName;

    @Value("${aws.game-bucket.name}")
    private String gameBucketName;

    @Value("${aws.image-bucket.name}")
    private String imageBucketName;

    private final S3Client s3Client;

    public GameManagerService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // Logic should only be applied to HTML5 based games
    public void extractZip(RequestData requestData) {

        String prefix = "games/" + requestData.gameProfileUUID() + "/files/" + requestData.fileName();

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


    public void transferGameImagesToS3(List<RequestData> imageData){

        for (int i = 0; imageData.size() > i; i++) {

            try {
                String prefix = "images/games/" + imageData.get(i).gameProfileUUID() + "/gameImages/" + imageData.get(i).fileName();

                CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                        .sourceBucket(validBucketName)
                        .sourceKey(prefix)
                        .destinationBucket(imageBucketName)
                        .destinationKey(prefix)
                        .build();

                s3Client.copyObject(copyRequest);
                System.out.println("File successfully copied to destination bucket.");

                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(validBucketName)
                        .key(prefix)
                        .build();

                s3Client.deleteObject(deleteRequest);
                System.out.println("Original file deleted from source bucket.");
            } catch (RuntimeException exception) {

                System.out.println("Failed to transfer image object: " + imageData.get(i).fileName() + " to destination bucket : " + exception.getMessage());
            }
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