package com.warpedcitadel.filemanagementservice.filemanagement.dto;

import com.warpedcitadel.filemanagementservice.enums.FileStatus;
import com.warpedcitadel.filemanagementservice.enums.PlatformOS;

import java.util.UUID;

public record SearchAttributesDto(
        String title,
        UUID gameProfileUUID,
        Integer statusType,
        Integer platformOS
) {
    public SearchAttributesDto {
        if (statusType == null || statusType > FileStatus.values().length || statusType < FileStatus.NEW.getCode()) {
            statusType = -1;
        }
        if (platformOS == null || platformOS > PlatformOS.values().length || platformOS < PlatformOS.BROWSER.getCode()) {
            platformOS = -1;
        }
    }
}
