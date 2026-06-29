package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.filemanager.model.FileMetaDataModel;
import com.warpedcitadel.fileuploadservice.filemanager.model.ImageMetaDataModel;
import com.warpedcitadel.fileuploadservice.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping(path = "/user", version = "1.0")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @PostMapping(value = "/upload/game", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadGameFile(@RequestPart("file")MultipartFile file,
                                                      @RequestPart("fileDetails") FileMetaDataModel fileDetails,
                                                      WebRequest request) throws IOException {
        fileService.uploadFileToS3(file, fileDetails);
        ApiResponse fileData = new ApiResponse<>("Upload", HttpStatus.CREATED.value(),
                "Uploaded game file",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.CREATED);
    }


    @PostMapping(value = "/upload/profile/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadProfileImage(@RequestPart("file")MultipartFile file,
                                                          @RequestPart("fileDetails") ImageMetaDataModel fileDetails,
                                                          WebRequest request) throws SQLException, IOException {
        fileService.uploadImageToS3(file, fileDetails);
        ApiResponse fileData = new ApiResponse<>("Update", HttpStatus.CREATED.value(),
                "Changed profile image",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.CREATED);
    }
}
