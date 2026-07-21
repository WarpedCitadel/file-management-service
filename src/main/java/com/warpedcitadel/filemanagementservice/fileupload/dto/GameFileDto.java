package com.warpedcitadel.filemanagementservice.fileupload.dto;

import org.springframework.web.multipart.MultipartFile;

public record GameFileDto(
    MultipartFile file,
    GameFileDetails fileDetails
) {}
