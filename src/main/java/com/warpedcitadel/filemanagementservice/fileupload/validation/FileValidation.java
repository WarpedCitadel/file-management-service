package com.warpedcitadel.filemanagementservice.fileupload.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileValidation {

    Logger log = LoggerFactory.getLogger(FileValidation.class);

    public boolean isValidFile(MultipartFile file, String[] applicableTypes, long maxSize) throws MaxUploadSizeExceededException {
        try {
            if (file != null) {
                if (checkFileSize(file, maxSize)) {
                    for (String applicableType : applicableTypes) {
                        String fileType = getFileExtension(file.getOriginalFilename());
                        if (fileType.equals(applicableType)) {
                            return true;
                        }
                    }
                }
                log.error("File {} content type: ({}) is unsupported", file.getOriginalFilename(), file.getContentType());
            }
        } catch (IOException exception) {
            log.error("Failed to get file {} size in bytes, exception: {}", file.getOriginalFilename(), exception.getMessage());
        }
        return false;
    }


    public boolean checkFileSize(MultipartFile file, long maxSize) throws IOException {

        try {
            if (file.getSize() > maxSize) {
                log.error("File {} content size ({} bytes) is too large", file.getOriginalFilename(), file.getBytes());
                throw new MaxUploadSizeExceededException(1);
            }
            return true;
        } catch (IOException exception) {
            throw exception;
        }
    }


    public String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
           return fileName.substring(lastDotIndex, fileName.length());
        }
        return "";
    }
}
