package com.warpedcitadel.filemanagementservice.fileupload.model;

public class FileMetaDataModel {

    private String gameProfileUUID;
    private String fileUUID;
    private long appUserID;
    private String appUserUUID;
    private String fileName;
    private int platformOS;
    private String fileSize;
    private String statusType;


    public FileMetaDataModel() {

    }

    public FileMetaDataModel(String fileUUID) {
        this.fileUUID= fileUUID;
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
    public String getFileUUID() {
        return fileUUID;
    }

    public long getAppUserID() {
        return appUserID;
    }

    public String getAppUserUUID() {
        return appUserUUID;
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

    public String getStatusType() {
        return statusType;
    }
}
