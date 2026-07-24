package com.warpedcitadel.filemanagementservice.filemanagement.dto;

import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;

public record FileDataDto(
        SlicedResponse<FileDataModel> files
) {}
