package com.warpedcitadel.filemanagementservice.filetransfer;

import com.warpedcitadel.filemanagementservice.filetransfer.dto.RequestData;
import com.warpedcitadel.filemanagementservice.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(path = "/api/transfer", version = "1.0")
public class FileTransferController {

    private final fileTransferService fileTransferService;

    public FileTransferController(fileTransferService fileTransferService) {
        this.fileTransferService = fileTransferService;
    }

    @PostMapping("/transferHtml5Game")
    public ResponseEntity<ApiResponse<String>> unzipFileToS3(@RequestBody RequestData requestData,
                                                                            WebRequest request) {

        fileTransferService.extractZip(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Transfer", HttpStatus.OK.value(),
                "Transferred file to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }


    @PostMapping("/transferGameFile")
    public ResponseEntity<ApiResponse<String>> transferGameFileToS3(@RequestBody List<RequestData> requestData,
                                                                                    WebRequest request) {

        fileTransferService.transferGameFileToS3(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Transfer", HttpStatus.OK.value(),
                "Transferred files to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }


    @PostMapping("/transferGameImage")
    public ResponseEntity<ApiResponse<String>> transferGameImageToS3(@RequestBody List<RequestData> requestData,
                                                                                    WebRequest request) {

        fileTransferService.transferGameImagesToS3(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Transfer", HttpStatus.OK.value(),
                "Transferred images to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }


    @PostMapping("/transferProfileImage")
    public ResponseEntity<ApiResponse<String>> transferProfileImageToS3(@RequestBody RequestData requestData,
                                                                                        WebRequest request) {

        fileTransferService.transferProfileImageToS3(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Transfer", HttpStatus.OK.value(),
                "Transferred image to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }
}
