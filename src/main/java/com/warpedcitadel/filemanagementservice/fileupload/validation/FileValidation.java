package com.warpedcitadel.filemanagementservice.fileupload.validation;

import com.warpedcitadel.filemanagementservice.fileupload.dto.GameFileDetails;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileValidation {

    Logger log = LoggerFactory.getLogger(FileValidation.class);

    public boolean isValidFile(MultipartFile file, String[] applicableTypes, long maxSize) throws IOException {
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
                log.error("File ({}) content type: ({}) is unsupported",
                        file.getOriginalFilename(), file.getContentType());
            }
        } catch (IOException exception) {
            log.error("File ({}) ({}) bytes file validation failed, Reason: ({})",
                    file.getOriginalFilename(), file.getBytes(), exception.getMessage());
        }
        return false;
    }


    public boolean checkFileSize(MultipartFile file, long maxSize) throws IOException {
        if (file.getSize() > maxSize) {
            log.error("File ({}) ({}) bytes exceeded maximum upload size",
                    file.getOriginalFilename(), file.getBytes());
            throw new MaxUploadSizeExceededException(maxSize);
        }
        return true;
    }


    public String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
           return fileName.substring(lastDotIndex, fileName.length());
        }
        return "";
    }

    public void checkFilePlatformOS(List<GameFileDetails> fileDetails) {
        List<GameFileDetails> browserList = new ArrayList<>();
        for (int i = 0; fileDetails.size() > i; i++) {
            if (fileDetails.get(i).platformOS() == 1) {
                browserList.add(fileDetails.get(i));
            }
        }
        if (browserList.size() > 1) {
            log.error("Expected one or zero HTML5 files to be upload but received ({}) for game profile ID: ({})",
                    browserList.size(), browserList.getFirst().gameProfileUUID());
            throw new IllegalArgumentException("Only one file can be uploaded as a HTML5 game");
        }
    }


    public void checkCoverImageCount(List<GameImageDetails> fileDetails) {
        List<GameImageDetails> imageList = new ArrayList<>();
        for (int i = 0; fileDetails.size() > i; i++) {
            if (fileDetails.get(i).isCover()) {
                imageList.add(fileDetails.get(i));
            }
        }
        if (imageList.size() > 1) {
            log.error("Expected one or zero cover images to be upload but received ({}) for game profile ID: ({})",
                    imageList.size(), imageList.getFirst().gameProfileUUID());
            throw new IllegalArgumentException("Only one file can be uploaded as a cover image game");
        }
    }
}
