package com.warpedcitadel.filemanagementservice.fileupload.model;

import java.util.UUID;

public class FileMetaDataModel {

    private UUID gameProfileUUID;
    private String fileName;
    private int platformOS;
    private String fileSize;


    public FileMetaDataModel() {

    }


    public FileMetaDataModel(UUID gameProfileUUID, String fileName, int platformOS, String fileSize) {
        this.gameProfileUUID = gameProfileUUID;
        this.fileName = fileName;
        this.platformOS = platformOS;
        this.fileSize = fileSize;
    }


    public UUID getGameProfileUUID() {
        return gameProfileUUID;
    }

    public String getFileName() {
        return fileName;
    }

    public int getPlatformOS() {
        return platformOS;
    }

    public String getFileSize() {
        return fileSize;
    }
}
