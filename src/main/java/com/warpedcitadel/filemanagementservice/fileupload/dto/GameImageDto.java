package com.warpedcitadel.filemanagementservice.fileupload.dto;

import org.springframework.web.multipart.MultipartFile;

public record GameImageDto(
        MultipartFile file,
        GameImageDetails details
) {}
