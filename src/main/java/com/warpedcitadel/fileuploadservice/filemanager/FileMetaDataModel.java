package com.warpedcitadel.fileuploadservice.filemanager;

public class FileMetaDataModel {

    private long id;
    private String fileUuid;
    private long appUserId;
    private String appUserUuid;
    private String fileName;
    private String fileVersion;
    private String fileSize;
    private String fileType;
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

    public FileMetaDataModel(long appUserId, String fileName, String fileVersion, String fileSize) {
        this.appUserId = appUserId;
        this.fileName = fileName;
        this.fileVersion = fileVersion;
        this.fileSize = fileSize;
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


    @Override
    public String toString() {
        return "FileMetaDataModel{" +
                "id=" + id +
                ", fileUuid='" + fileUuid + '\'' +
                ", appUserId=" + appUserId +
                ", fileName='" + fileName + '\'' +
                ", fileVersion='" + fileVersion + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", statusType='" + statusType + '\'' +
                '}';
    }
}
