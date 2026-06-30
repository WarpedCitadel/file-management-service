package com.warpedcitadel.fileuploadservice.gamemanager;

import com.warpedcitadel.fileuploadservice.gamemanager.dto.CloudFrontCookie;
import com.warpedcitadel.fileuploadservice.gamemanager.dto.RequestData;
import com.warpedcitadel.fileuploadservice.gamemanager.dto.ResponseData;
import com.warpedcitadel.fileuploadservice.gamemanager.util.CloudFrontCookieMaker;
import com.warpedcitadel.fileuploadservice.payload.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.time.Clock;
import java.time.Instant;

@RestController
@RequestMapping(path = "/api/games", version = "1.0")
public class GameManagerController {

    private final GameManagerService gameManagerService;
    private final CloudFrontCookieMaker cloudFrontCookieMaker;

    public GameManagerController(GameManagerService gameManagerService,
                                 CloudFrontCookieMaker cloudFrontCookieMaker) {

        this.gameManagerService = gameManagerService;
        this.cloudFrontCookieMaker = cloudFrontCookieMaker;
    }


    @GetMapping("/getGame")
    public ResponseEntity<ApiResponse<ResponseData>> requestGameUrl(@RequestBody RequestData requestData,
                                                                    WebRequest request,
                                                                    HttpServletResponse response) {

        ResponseData responseData = gameManagerService.generateHtmlGameUrl(requestData);
        CloudFrontCookie cookie = cloudFrontCookieMaker.generateSignedCookie(requestData);

        addCookie(response,
                "CloudFront-Policy",
                cookie.policy());

        addCookie(response,
                "CloudFront-Signature",
                cookie.signature());

        addCookie(response,
                "CloudFront-Key-Pair-Id",
                cookie.keyPairId());

        ApiResponse<ResponseData> fileData = new ApiResponse<>("Request Game Url", HttpStatus.OK.value(),
                responseData,
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }


    @PostMapping("/uploadGame")
    public ResponseEntity<ApiResponse<String>> uploadGameToS3(@RequestBody RequestData requestData, WebRequest request) {


        gameManagerService.extractZip(requestData);
        ApiResponse<String> fileData = new ApiResponse<>("Upload", HttpStatus.OK.value(),
                "Uploaded to S3",
                request.getDescription(false).replace("uri=", ""),
                Instant.now(Clock.systemUTC()));
        return new ResponseEntity<>(fileData, HttpStatus.OK);
    }

    // ### HELPER Function ###
    private void addCookie(
            HttpServletResponse response,
            String name,
            String value) {

        ResponseCookie cookie =
                ResponseCookie
                        .from(name, value)
                        .secure(true)
                        .httpOnly(true)
                        .sameSite("None")
                        .path("/")
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }


}
