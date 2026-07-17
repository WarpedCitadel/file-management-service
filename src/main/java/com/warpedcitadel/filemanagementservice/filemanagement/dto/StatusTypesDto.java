package com.warpedcitadel.filemanagementservice.filemanagement.dto;

import java.util.HashMap;

public record StatusTypesDto(
        HashMap<Integer, String> statusTypes
) {}
