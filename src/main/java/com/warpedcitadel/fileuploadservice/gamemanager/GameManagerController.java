package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import com.warpedcitadel.fileuploadservice.gamemanager.dto.ResponseData;
import com.warpedcitadel.fileuploadservice.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping(path = "/game", version = "1.0")
public class GameManagerController {

    private final GameManagerService gameManagerService;

    public GameManagerController(GameManagerService gameManagerService) {
        this.gameManagerService = gameManagerService;
    }


    @GetMapping("/presignedUrl")
    public ResponseEntity<ApiResponse<ResponseData>> requestPresignedUrl(@RequestBody RequestData requestData, WebRequest request) {


        ResponseData responseData = gameManagerService.generatePresignedUrl(requestData);
        ApiResponse fileData = new ApiResponse<>("Request Url", HttpStatus.OK.value(),
                responseData,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }

}
