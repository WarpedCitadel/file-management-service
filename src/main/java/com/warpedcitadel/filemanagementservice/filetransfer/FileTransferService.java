package com.warpedcitadel.filemanagementservice.filetransfer;

import com.warpedcitadel.filemanagementservice.filetransfer.dto.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FileTransferService {

    @Value("${aws.valid-bucket.name}")
    private String validBucketName;
    @Value("${aws.game-bucket.name}")
    private String gameBucketName;
    @Value("${aws.image-bucket.name}")
    private String imageBucketName;

    private final S3Client s3Client;
    private static final Logger log = LoggerFactory.getLogger(FileTransferService.class);

    public FileTransferService(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    public void extractZip(RequestData requestData) {
        long start = System.currentTimeMillis();
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
                                    .contentEncoding(getContentEncoding(entry.getName()))
                                    .build();
                    s3Client.putObject(
                            put,
                            RequestBody.fromBytes(byteArrayOutputStream.toByteArray())
                    );
                    zip.closeEntry();
                    long elapsed = System.currentTimeMillis() - start;
                    log.info("Unzipped and transferred file ({}) contents from ({}) bucket to ({}) bucket in ({}) ms for game profile ID: ({})",
                    requestData.fileName(), validBucketName, gameBucketName, elapsed, requestData.objectUUID());
            }
        } catch (IOException | S3Exception exception) {
                log.error("Failed to unzip and transfer ({}) file contents from ({}) bucket to ({}) bucket for game profile ID: ({}) Reason: ({})",
                        requestData.fileName(), validBucketName, gameBucketName, requestData.objectUUID(), exception.toString());
                throw new RuntimeException("Failed transfer file contents of "
                    + requestData.fileName()
                    + " to storage");
        }
    }


    public void transferGameFilesToS3(List<RequestData> requestData) {
        long elapsedTotal = 0L;
        for (int i = 0; requestData.size() > i; i++) {
            long start = System.currentTimeMillis();
            try {
                String prefix = "games/" + requestData.get(i).objectUUID() + "/files/" + requestData.get(i).fileName();
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
                long elapsed = System.currentTimeMillis() - start;
                elapsedTotal += elapsed;
                log.info("Transferred file ({}) from ({}) bucket to ({}) bucket in ({}) ms for game profile ID: ({})",
                        requestData.get(i).fileName(), validBucketName, gameBucketName, elapsed, requestData.getFirst().objectUUID());
            } catch (S3Exception exception) {
                log.error("Failed to transfer ({}) file contents from ({}) bucket to ({}) bucket for game profile ID: ({}) Reason: ({})",
                        requestData.get(i).fileName(), validBucketName, gameBucketName, requestData.get(i).objectUUID(), exception.toString());
                throw new RuntimeException("Failed transfer file contents of "
                        + requestData.get(i).fileName()
                        + " to storage");
            }
        }
        log.info("Transferred ({}) files from ({}) bucket to ({}) bucket in ({}) ms for game profile ID: ({})",
                requestData.size(), validBucketName, gameBucketName, elapsedTotal, requestData.getFirst().objectUUID());
    }


    public void transferGameImagesToS3(List<RequestData> requestData) {
        long elapsedTotal = 0L;
        for (int i = 0; requestData.size() > i; i++) {
            long start = System.currentTimeMillis();
            try {
                String prefix = "images/games/" + requestData.get(i).objectUUID() + "/gameImages/" + requestData.get(i).fileName();
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
                long elapsed = System.currentTimeMillis() - start;
                elapsedTotal += elapsed;
                log.info("Transferred file ({}) from ({}) bucket to ({}) bucket in ({}) ms for game profile ID: ({})",
                        requestData.get(i).fileName(), validBucketName, imageBucketName, elapsed, requestData.getFirst().objectUUID());
            } catch (S3Exception exception) {
                log.error("Failed to transfer ({}) file contents from ({}) bucket to ({}) bucket for game profile ID: ({}) Reason: ({})",
                        requestData.get(i).fileName(), validBucketName, imageBucketName, requestData.get(i).objectUUID(), exception.toString());
                throw new RuntimeException("Failed transfer file contents of "
                        + requestData.get(i).fileName()
                        + " to storage");
            }
        }
        log.info("Transferred ({}) files from ({}) bucket to ({}) bucket in ({}) ms for game profile ID: ({})",
                requestData.size(), validBucketName, imageBucketName, elapsedTotal, requestData.getFirst().objectUUID());
    }


    public void transferProfileImageToS3(RequestData requestData){
            try {
                long start = System.currentTimeMillis();
                String prefix = "images/users/" + requestData.objectUUID() + "/image/" + requestData.fileName();
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
                long elapsed = System.currentTimeMillis() - start;
                log.info("Transferred file ({}) from ({}) bucket to ({}) bucket in ({}) ms for game profile ID: ({})",
                        requestData.fileName(), validBucketName, imageBucketName, elapsed, requestData.objectUUID());
            } catch (S3Exception exception) {
                log.error("Failed to transfer ({}) file content from ({}) bucket to ({}) bucket for user profile ID: ({}) Reason: ({})",
                        requestData.fileName(), validBucketName, imageBucketName, requestData.objectUUID(), exception.toString());
                throw new RuntimeException("Failed transfer file content of "
                        + requestData.fileName()
                        + " to storage");
        }
    }


    private String getContentType(String fileName) {
        if (fileName.endsWith(".gz") || fileName.endsWith(".br")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return switch (fileName.substring(fileName.lastIndexOf('.') + 1)) {
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


    private String getContentEncoding(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".gz") || lowerName.contains("-gzip")) return "gzip";
        if (lowerName.endsWith(".br") || lowerName.contains("-brotli")) return "br";
        return null;
    }
}