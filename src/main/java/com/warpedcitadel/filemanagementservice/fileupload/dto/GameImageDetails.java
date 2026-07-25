package com.warpedcitadel.filemanagementservice.fileupload.dto;

import java.util.UUID;

public record GameImageDetails(
        UUID gameProfileUUID,
        boolean isCover
) {}
