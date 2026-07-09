package com.warpedcitadel.fileuploadservice.filemanager.model;

public class FileMetaDataModel {

    private String gameProfileUUID;
    private String fileUUID;
    private long appUserID;
    private String appUserUUID;
    private String fileName;
    private String fileVersion;
    private String fileSize;
    private String statusType;


    public FileMetaDataModel() {

    }

    public FileMetaDataModel(String fileUUID) {
        this.fileUUID= fileUUID;
    }

    public FileMetaDataModel(String appUserUUID, String fileVersion){
        this.appUserUUID = appUserUUID;
        this.fileVersion = fileVersion;
    }

    public FileMetaDataModel(String gameProfileUUID, String fileName, String fileVersion, String fileSize) {
        this.gameProfileUUID = gameProfileUUID;
        this.fileName = fileName;
        this.fileVersion = fileVersion;
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

    public String getFileVersion() {
        return fileVersion;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getStatusType() {
        return statusType;
    }
}
