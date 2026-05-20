package com.warpedcitadel.fileuploadservice.filemanager;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/file", version = "1.0")
public class FileController {


    @Autowired
    private FileService fileService;


    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file,
                                             @RequestPart("fileDetails") FileMetaDataModel fileDetails) {

        fileService.uploadFileToS3(file, fileDetails);
        return ResponseEntity.ok("File uploaded successfully!");
    }

}
