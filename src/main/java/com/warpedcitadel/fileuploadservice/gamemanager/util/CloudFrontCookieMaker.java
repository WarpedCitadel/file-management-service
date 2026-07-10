package com.warpedcitadel.fileuploadservice.gamemanager.util;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.CloudFrontCookie;
import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

// TODO: Move to content-management-service
@Service
public class CloudFrontCookieMaker {

    @Value("classpath:keys/private_key.pem")
    private Resource privateKeyResource;

    @Value("${cloud.aws.keypair}")
    private String keyPair;

    public CloudFrontCookie generateSignedCookie(RequestData requestData) {

        try {

            Instant expiration =
                    Instant.now().plus(Duration.ofHours(2));

            String resource =
                    "Https://www.warpedcitadel.com" +
                            "/html/" +
                            requestData.fileUUID() +
                            "/*";

            String policy =
                    createPolicy(resource, expiration);

            String signature =
                    sign(policy);

            return new CloudFrontCookie(
                    cloudFrontBase64(policy.getBytes(StandardCharsets.UTF_8)),
                    signature,
                    keyPair
            );

        } catch (Exception exception) {

            throw new RuntimeException("Failed generating CloudFront cookie", exception);
        }
    }


    private String createPolicy(
            String resource,
            Instant expiration) {

        return """
        {
          "Statement":[
            {
              "Resource":"%s",
              "Condition":{
                "DateLessThan":{
                  "AWS:EpochTime":%d
                }
              }
            }
          ]
        }
        """.formatted(
                resource,
                expiration.getEpochSecond());
    }


    private String sign(String policy) {

        try {

            Signature signer =
                    Signature.getInstance("SHA1withRSA");

            signer.initSign(loadPrivateKey());

            signer.update(
                    policy.getBytes(StandardCharsets.UTF_8));

            return cloudFrontBase64(
                    signer.sign());

        } catch (Exception exception) {

            throw new RuntimeException("Failed to create signature for cookies", exception);
        }
    }


    private PrivateKey loadPrivateKey() {

        try {

        String key =
                Files.readString(
                        privateKeyResource.getFile().toPath());

        key = key
                .replace(
                        "-----BEGIN PRIVATE KEY-----",
                        "")
                .replace(
                        "-----END PRIVATE KEY-----",
                        "")
                .replaceAll("\\s", "");

        byte[] decoded =
                Base64.getDecoder().decode(key);

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(decoded);

        return KeyFactory
                .getInstance("RSA")
                .generatePrivate(spec);
        } catch (Exception exception) {

            throw new RuntimeException("Failed to load keys", exception);
        }
    }


    private String cloudFrontBase64(byte[] bytes) {

        return Base64.getEncoder()
                .encodeToString(bytes)
                .replace('+', '-')
                .replace('=', '_')
                .replace('/', '~');
    }
}
