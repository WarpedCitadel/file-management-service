package com.warpedcitadel.filemanagementservice.exceptionhandlers;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        String title,
        int status,
        Map<String, String> errors,
        String instance,
        Instant timestamp
) {}