package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    private final S3Client s3Client;

    public GameManagerService(S3Client s3Client) {
        this.s3Client = s3Client;
    }


//    public ResponseData generatePresignedUrl(RequestData requestData) {
//
//        try (S3Presigner presigner = S3Presigner.builder().region(Region.of(region)).build()) {
//
//            GetObjectRequest objectRequest = GetObjectRequest.builder()
//                    .bucket(gameBucketName)
//                    .key("games/" + requestData.fileUUID())
//                    .build();
//
//            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
//                    .signatureDuration(Duration.ofMinutes(15))
//                    .getObjectRequest(objectRequest)
//                    .build();
//
//            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
//
//            ResponseData response = new ResponseData(
//                    presignedRequest.url().toString()
//            );
//
//            return response;
//        } catch (Exception exception) {
//
//            throw new RuntimeException("Failed to generate presigned url for object key of " +
//                    requestData.fileUUID(), exception);
//        }
//    }


    public void extractZip(RequestData requestData) {

        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(
                GetObjectRequest
                        .builder()
                        .bucket(validBucketName)
                        .key("games/" + requestData.fileUUID())
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
                                    .key("games/" + requestData.fileUUID() + "/" + entry.getName())
                                    .contentType(getContentType(entry.getName()))
                                    .build();

                    s3Client.putObject(
                            put,
                            RequestBody.fromBytes(byteArrayOutputStream.toByteArray())
                    );

                    zip.closeEntry();
            }

        } catch (IOException exception) {

            System.out.println("Failed to get zip stream: " + exception);
        }
    }

    // ## Helper functions
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
