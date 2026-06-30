package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import com.warpedcitadel.fileuploadservice.gamemanager.dto.ResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    private final S3Client s3Client;

    public GameManagerService(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    public ResponseData generateHtmlGameUrl(RequestData requestData) {

        try {

            String result = findFilesByExtension(requestData);

            String gameUrl = "https://www.warpedcitadel.com/" + result;

            ResponseData response = new ResponseData(
                    gameUrl
            );

            return response;
        } catch (Exception exception) {

            throw new RuntimeException("Failed to generate url for object key of " +
                    requestData.fileUUID(), exception);
        }
    }


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
                                    .key("html/" + requestData.fileUUID() + "/" + entry.getName())
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


    private String findFilesByExtension(RequestData requestData) {

        String filePath = "html/" + requestData.fileUUID();
        String prefix = filePath.endsWith("/") ? filePath : filePath + "/";

        List<String> fileKeys = new ArrayList<>();

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(gameBucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Iterable responses = s3Client.listObjectsV2Paginator(request);

        responses.contents().stream()
                .map(s3Object -> s3Object.key())
                .filter(key -> key.toLowerCase().endsWith(".html"))
                .forEach(htmlKey -> {
                    fileKeys.add(htmlKey);
                });

        return fileKeys.getFirst();
    }
}