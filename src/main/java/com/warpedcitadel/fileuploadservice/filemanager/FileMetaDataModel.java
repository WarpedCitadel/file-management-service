package com.warpedcitadel.fileuploadservice.filemanager;

public class FileMetaDataModel {

    private String gameProfileUUID;
    private String fileUuid;
    private long appUserId;
    private String appUserUuid;
    private String fileName;
    private String fileVersion;
    private String fileSize;
    private String statusType;


    public FileMetaDataModel() {

    }

    public FileMetaDataModel(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    public FileMetaDataModel(String appUserUuid, String fileVersion){
        this.appUserUuid = appUserUuid;
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
    public String getFileUuid() {
        return fileUuid;
    }

    public long getAppUserId() {
        return appUserId;
    }

    public String getAppUserUuid() {
        return appUserUuid;
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
