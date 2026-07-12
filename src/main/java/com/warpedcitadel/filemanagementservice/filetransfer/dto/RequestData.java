package com.warpedcitadel.filemanagementservice.filetransfer.dto;

public record RequestData(
        String gameProfileUUID,
        String fileName
) {}
