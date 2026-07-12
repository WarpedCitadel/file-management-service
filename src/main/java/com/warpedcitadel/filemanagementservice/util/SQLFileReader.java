package com.warpedcitadel.filemanagementservice.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SQLFileReader {

    public String loadSQL(String fileName) {
        try {

            ClassPathResource resource = new ClassPathResource("sql" + fileName);
            InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        } catch (IOException invalidSqlFile) {
            throw new RuntimeException("Could not read sql file: " + fileName, invalidSqlFile);
        }
    }
}
