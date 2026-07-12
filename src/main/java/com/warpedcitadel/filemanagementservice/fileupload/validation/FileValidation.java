package com.warpedcitadel.filemanagementservice.fileupload.validation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidation {

    public Boolean isValidFile(MultipartFile file, String[] applicableTypes, int maxSize) throws MaxUploadSizeExceededException {

        if (file == null) return false;

        if (!checkFileSize(file, maxSize)) throw new MaxUploadSizeExceededException(1);

        for (String applicableType : applicableTypes) {

            String fileType = getFileExtension(file.getOriginalFilename());
            if (fileType.equals(applicableType)) {

                return true;
            }
        }

        return false;
    }


    public Boolean checkFileSize(MultipartFile file, int maxSize) {
        return file.getSize() < maxSize;
    }

    public String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
           return fileName.substring(lastDotIndex, fileName.length());
        }

        return "";
    }
}
