package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import com.warpedcitadel.fileuploadservice.gamemanager.dto.ResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class GameManagerService {

    @Value("${cloud.aws.region}")
    private String region;

    @Value("${aws.game-bucket.name}")
    private String bucketName;

    private final S3Client s3Client;

    public GameManagerService(S3Client s3Client) {
        this.s3Client = s3Client;
    }


    public ResponseData generatePresignedUrl(RequestData requestData) {

        try (S3Presigner presigner = S3Presigner.builder().region(Region.of(region)).build()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(requestData.fileUUID())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            ResponseData response = new ResponseData(
                    presignedRequest.url().toString()
            );

            return response;
        } catch (Exception exception) {

            throw new RuntimeException("Failed to generate presigned url for object key of " +
                    requestData.fileUUID(), exception);
        }
    }
}
