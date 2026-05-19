package com.warpedcitadel.fileuploadservice.filemanager;

public class FileMetaDataModel {

    private long id;
    private String fileUuid;
    private long AppUserId;
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

    public FileMetaDataModel(String fileUuid, String fileName, String statusType){
        this.fileName =fileName;
        this.fileUuid = fileUuid;
        this.statusType = statusType;
    }

    public FileMetaDataModel(long appUserId, String fileName, String fileVersion, String fileSize) {
        this.AppUserId = appUserId;
        this.fileName = fileName;
        this.fileVersion = fileVersion;
        this.fileSize = fileSize;
    }


    public String getFileUuid() {
        return fileUuid;
    }

    public long getAppUserId() {
        return AppUserId;
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
                ", AppUserId=" + AppUserId +
                ", fileName='" + fileName + '\'' +
                ", fileVersion='" + fileVersion + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", statusType='" + statusType + '\'' +
                '}';
    }
}
