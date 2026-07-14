package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.filemanagement.dto.FileDataDto;
import com.warpedcitadel.filemanagementservice.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping(path = "/api/file", version = "1.0")
public class FileManagementController {

    private final FileManagementService fileManagementService;

    public FileManagementController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }

    @GetMapping("/getFiles/{uuid}")
    public ResponseEntity<ApiResponse<FileDataDto>> getGameFiles(@PathVariable String uuid,
                                                                                WebRequest request) {

         FileDataDto fileList = fileManagementService.getGameFiles(uuid);
        ApiResponse<FileDataDto> gameProfileDetails = new ApiResponse<>("Game profile details",
                HttpStatus.OK.value(),
                fileList,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(gameProfileDetails, HttpStatus.OK);
    }

}
