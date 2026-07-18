package com.warpedcitadel.filemanagementservice.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class VirusScanService {

    @Value("${clamav.host}")
    private String host;

    @Value("${clamav.port}")
    private int port;

    @Value("${clamav.timeout}")
    private int timeout;

    private static final Logger log = LoggerFactory.getLogger(VirusScanService.class);


    private String virusScan(MultipartFile file) throws IOException {

        InputStream inputStream = file.getInputStream();

        long start = System.currentTimeMillis();

        try (Socket socket = new Socket(host, port)) {

            socket.setSoTimeout(timeout);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));

            byte[] buffer = new byte[8192];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {

                byte[] size = ByteBuffer.allocate(4)
                        .putInt(read)
                        .array();

                out.write(size);
                out.write(buffer, 0, read);
            }

            out.write(new byte[] {0,0,0,0});
            out.flush();

            String response = new String(in.readAllBytes(), StandardCharsets.US_ASCII);

            long elapsed = System.currentTimeMillis() - start;

            log.info("Scanned {} ({} bytes) in {} ms - Clean: {}",
                    file.getOriginalFilename(),
                    file.getSize(),
                    elapsed,
                    response);

            return response;
        }
    }

    public boolean processFile(MultipartFile file) {

        try {
            String result = virusScan(file);

            if (result.contains("OK")) {

                System.out.println("File is clean. Processing continued.");
                return true;
            } else if (result.contains("FOUND")) {

                System.out.println("Malware detected. Processing canceled.");
                return false;
            } else {

                System.out.println("Error occurred. Processing canceled.");
                return false;
            }
        } catch (Exception exception) {

            System.out.println("Error occurred. Processing canceled.");
            return false;
        }
    }
}
