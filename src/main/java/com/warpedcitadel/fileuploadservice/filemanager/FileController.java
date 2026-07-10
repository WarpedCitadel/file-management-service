package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.filemanager.dto.GameImageDetails;
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
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(path = "/api/user", version = "1.0")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    @PostMapping(value = "/upload/game", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadGameFile(@RequestPart("file") MultipartFile file,
                                                              @RequestPart("fileDetails") FileMetaDataModel fileDetails,
                                                              WebRequest request) throws IOException {
        fileService.uploadFileToS3(file, fileDetails);
        ApiResponse<String> fileData = new ApiResponse<>("Upload", HttpStatus.CREATED.value(),
                "Uploaded game file",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.CREATED);
    }


    @PostMapping(value = "/upload/profile/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@RequestPart("file") MultipartFile file,
                                                                  @RequestPart("fileDetails") ImageMetaDataModel fileDetails,
                                                                  WebRequest request) throws IOException {
        fileService.uploadImageToS3(file, fileDetails);
        ApiResponse<String> fileData = new ApiResponse<>("Update", HttpStatus.OK.value(),
                "Changed profile image",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }


    @PostMapping(value = "/upload/game/img", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@RequestPart("file") List<MultipartFile> file,
                                                                  @RequestPart("details") List<GameImageDetails> details,
                                                                  WebRequest request) throws IOException {

        fileService.uploadGameImageToS3(file, details);
        ApiResponse<String> fileData = new ApiResponse<>("Upload", HttpStatus.OK.value(),
                "Uploaded game profile images",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }
}
