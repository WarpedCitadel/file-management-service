package com.warpedcitadel.filemanagementservice.filetransfer.dto;

public record RequestData(
        String objectUUID,
        String fileName
) {}
