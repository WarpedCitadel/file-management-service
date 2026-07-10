package com.warpedcitadel.fileuploadservice.filemanager.dto;

import org.springframework.web.multipart.MultipartFile;

public record GameImageDto(
        MultipartFile file,
        GameImageDetails details
) {}
