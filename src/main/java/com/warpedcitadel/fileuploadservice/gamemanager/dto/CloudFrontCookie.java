package com.warpedcitadel.fileuploadservice.gamemanager.dto;

public record CloudFrontCookie(
        String policy,
        String signature,
        String keyPairId
) {}
