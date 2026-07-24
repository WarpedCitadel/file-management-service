package com.warpedcitadel.filemanagementservice.filemanagement.dto;

import java.util.UUID;

public record FileDto(
        UUID gameProfileUUID,
        UUID fileUUID,
        int fileStatus
) {}
