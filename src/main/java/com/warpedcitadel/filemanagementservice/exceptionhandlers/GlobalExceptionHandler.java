package com.warpedcitadel.filemanagementservice.exceptionhandlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
    public ApiErrorResponse IOExceptions(IOException ioException, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", ioException.getMessage());
        return new ApiErrorResponse(
                "File error",
                HttpStatus.BAD_REQUEST.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SQLException.class)
    public ApiErrorResponse SQLExceptions(SQLException sqlException, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", sqlException.getMessage());
        return new ApiErrorResponse(
                "Database error",
                HttpStatus.BAD_REQUEST.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }


    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiErrorResponse maxUploadSizeExceededException(MaxUploadSizeExceededException exception, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", exception.getMessage());
        return new ApiErrorResponse(
                "File content too large",
                HttpStatus.CONTENT_TOO_LARGE.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiErrorResponse illegalArgumentException(IllegalArgumentException exception, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", exception.getMessage());
        return new ApiErrorResponse(
                "Invalid input",
                HttpStatus.BAD_REQUEST.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(S3Exception.class)
    public ApiErrorResponse S3Exception(S3Exception exception, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", exception.getMessage());
        return new ApiErrorResponse(
                "Object storage failure",
                HttpStatus.BAD_REQUEST.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public ApiErrorResponse RuntimeException(Exception exception, WebRequest request){
        Map<String, String> errors = new HashMap<>();
        errors.put("Message", exception.getMessage());
        return new ApiErrorResponse(
                "Error",
                HttpStatus.BAD_REQUEST.value(),
                errors,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC())
        );
    }
}
