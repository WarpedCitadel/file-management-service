package com.warpedcitadel.fileuploadservice.filemanager;

import java.util.UUID;

public class FileModel {

    private long id;
    private UUID uuid;
    private long appUserId;
    private String fileName;
    private String fileURL;
    private String fileVersion;
    private String fileSize;
    private String fileType;
    private int Status;


    public FileModel(long appUserId, String fileName, String fileURL, String fileVersion, String fileSize, String fileType, int status) {
        this.appUserId = appUserId;
        this.fileName = fileName;
        this.fileURL = fileURL;
        this.fileVersion = fileVersion;
        this.fileSize = fileSize;
        this.fileType = fileType;
        Status = status;
    }


    public long getAppUserId() {
        return appUserId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileURL() {
        return fileURL;
    }

    public String getFileVersion() {
        return fileVersion;
    }


    public String getFileSize(){
        return fileSize;
    }


    public String getFileType() {
        return fileType;
    }

    public int getStatus() {
        return Status;
    }


    public void setAppUserId(long appUserId) {
        this.appUserId = appUserId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public void setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
    }

    public void setFileSize(String fileSize){
        this.fileVersion = fileSize;
    }

    public void setFileType(String fileType) {
        fileType = fileType;
    }

    public void setStatus(int status) {
        Status = status;
    }


    @Override
    public String toString() {
        return "FileModel{" +
                "appUserId=" + appUserId +
                ", fileName='" + fileName + '\'' +
                ", fileURL='" + fileURL + '\'' +
                ", fileVersion='" + fileVersion + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", FileType='" + fileType + '\'' +
                ", Status=" + Status +
                '}';
    }
}
