package com.warpedcitadel.filemanagementservice.fileupload.dto;

public record FileUploadDto(
        String gameProfileUUID,
        String fileVersion,
        int platformOS
) {}
