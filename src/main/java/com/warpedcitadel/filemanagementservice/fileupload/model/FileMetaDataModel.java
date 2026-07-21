package com.warpedcitadel.filemanagementservice.fileupload.model;

public class FileMetaDataModel {

    private String gameProfileUUID;
    private String fileName;
    private int platformOS;
    private String fileSize;


    public FileMetaDataModel() {

    }


    public FileMetaDataModel(String gameProfileUUID, String fileName, int platformOS, String fileSize) {
        this.gameProfileUUID = gameProfileUUID;
        this.fileName = fileName;
        this.platformOS = platformOS;
        this.fileSize = fileSize;
    }


    public String getGameProfileUUID() {
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
