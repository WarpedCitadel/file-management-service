package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.filemanagement.dto.FileDataDto;
import com.warpedcitadel.filemanagementservice.filemanagement.dto.FileDto;
import com.warpedcitadel.filemanagementservice.filemanagement.dto.SearchAttributesDto;
import com.warpedcitadel.filemanagementservice.filemanagement.dto.StatusTypesDto;
import com.warpedcitadel.filemanagementservice.payload.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(path = "/api/file", version = "1.0")
public class FileManagementController {

    private final FileManagementService fileManagementService;

    public FileManagementController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }


    @GetMapping("/getFiles")
    public ResponseEntity<ApiResponse<FileDataDto>> getFiles(SearchAttributesDto attributes,
                                                                          Pageable pageable, WebRequest request) {

        FileDataDto files = fileManagementService.getFiles(pageable, attributes);
        ApiResponse<FileDataDto> response = new ApiResponse<>("File management list",
                HttpStatus.OK.value(),
                files,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/updateFileStatus")
    public ResponseEntity<ApiResponse> updateFileStatus(@RequestBody List<FileDto> files, WebRequest request) {
        fileManagementService.updateFileStatus(files);
        ApiResponse<String> gameProfileDetails = new ApiResponse<>("Update",
                HttpStatus.OK.value(),
                "Files Statuses Updated",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(gameProfileDetails, HttpStatus.OK);
    }


    @GetMapping("/getStatusTypes")
    public ResponseEntity<ApiResponse<StatusTypesDto>> getStatusTypes(WebRequest request) {
        StatusTypesDto statusTypes = fileManagementService.getStatusTypes();
        ApiResponse<StatusTypesDto> response = new ApiResponse<>("Status Types",
                HttpStatus.OK.value(),
                statusTypes,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return  new ResponseEntity<>(response, HttpStatus.OK);
    }
}
