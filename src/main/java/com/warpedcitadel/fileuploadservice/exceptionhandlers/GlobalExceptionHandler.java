package com.warpedcitadel.fileuploadservice.exceptionhandlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IOException.class)
    public ApiErrorResponse handleFailedUploadExceptions(IOException ioException, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", ioException.getMessage());
        return new ApiErrorResponse(
                "Failed upload",
                HttpStatus.BAD_REQUEST.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(SQLException.class)
    public ApiErrorResponse handleCanNotFindObjectExceptions(SQLException sqlException, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", sqlException.getMessage());
        return new ApiErrorResponse(
                "Failed upload",
                HttpStatus.NOT_FOUND.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }

    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiErrorResponse maxUploadSizeExceededException(MaxUploadSizeExceededException MUSEException, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", MUSEException.getMessage());
        return new ApiErrorResponse(
                "File content too large",
                HttpStatus.CONTENT_TOO_LARGE.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }
}
