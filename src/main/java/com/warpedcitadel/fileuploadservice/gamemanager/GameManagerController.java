package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import com.warpedcitadel.fileuploadservice.payload.ApiResponse;
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
public class GameManagerController {

    private final GameManagerService gameManagerService;

    public GameManagerController(GameManagerService gameManagerService) {
        this.gameManagerService = gameManagerService;
    }

    @PostMapping("/transferHtml5Game")
    public ResponseEntity<ApiResponse<String>> transferGameFileToS3(@RequestBody RequestData requestData, WebRequest request) {


        gameManagerService.extractZip(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Upload", HttpStatus.OK.value(),
                "Uploaded to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }


    @PostMapping("/transferGameImage")
    public ResponseEntity<ApiResponse<String>> transferGameImageToS3(@RequestBody List<RequestData> requestData, WebRequest request) {


        gameManagerService.transferGameImagesToS3(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Upload", HttpStatus.OK.value(),
                "Transferred to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }
}
