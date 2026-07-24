package com.warpedcitadel.filemanagementservice.filemanagement.model;

import java.util.UUID;

public class FileDataModel {

    long appUserID;
    UUID gameProfileUUID;
    String title;
    UUID fileUUID;
    String fileName;
    String fileSize;
    Integer platformOS;
    Integer fileStatus;
    String createdDtm;


    public FileDataModel() {

    }


    public FileDataModel(long appUserID, UUID gameProfileUUID, String title,
                         UUID fileUUID, String fileName, String fileSize,
                         Integer platformOS, Integer fileStatus, String createdDtm) {

        this.appUserID = appUserID;
        this.gameProfileUUID = gameProfileUUID;
        this.title = title;
        this.fileUUID = fileUUID;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.platformOS = platformOS;
        this.fileStatus = fileStatus;
        this.createdDtm = createdDtm;
    }


    public long getAppUserID() {
        return appUserID;
    }

    public UUID getGameProfileUUID() {
        return gameProfileUUID;
    }

    public String getTitle() {
        return title;
    }

    public UUID getFileUUID() {
        return fileUUID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public Integer getPlatformOS() {
        return platformOS;
    }

    public Integer getFileStatus() {
        return fileStatus;
    }

    public String getCreatedDtm() {
        return createdDtm;
    }


    @Override
    public String toString() {
        return "FileDataModel{" +
                "appUserID=" + appUserID +
                ", gameProfileUUID=" + gameProfileUUID +
                ", title='" + title + '\'' +
                ", fileUUID=" + fileUUID +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", platformOS=" + platformOS +
                ", fileStatus=" + fileStatus +
                ", createdDtm='" + createdDtm + '\'' +
                '}';
    }
}
