package com.warpedcitadel.filemanagementservice.filemanagement.dto;

public record FileRequestDto(
        String gameProfileUUID,
        int fileStatus
) {}
