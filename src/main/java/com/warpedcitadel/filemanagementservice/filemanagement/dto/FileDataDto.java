package com.warpedcitadel.filemanagementservice.filemanagement.dto;

import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;

import java.util.List;

public record FileDataDto(
        List<FileDataModel> fileData
) {}
