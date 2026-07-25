package com.warpedcitadel.filemanagementservice.filetransfer.dto;

import java.util.UUID;

public record RequestData(
        UUID objectUUID,
        String fileName
) {}
