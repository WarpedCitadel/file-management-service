package com.warpedcitadel.filemanagementservice.fileupload.dto;

import java.util.UUID;

public record GameFileDetails(
        UUID gameProfileUUID,
        int platformOS
) {}
