package com.warpedcitadel.filemanagementservice.filemanagement.model;

public class FileDataModel {

    String fileName;
    String fileVersion;
    String fileSize;
    boolean isBrowser;
    int fileStatus;
    String modifiedDtm;
    String createdDtm;

    public FileDataModel() {

    }

    public FileDataModel(String fileName, String fileVersion, String fileSize, boolean isBrowser, int fileStatus, String modifiedDtm, String createdDtm) {
        this.fileName = fileName;
        this.fileVersion = fileVersion;
        this.fileSize = fileSize;
        this.isBrowser = isBrowser;
        this.fileStatus = fileStatus;
        this.modifiedDtm = modifiedDtm;
        this.createdDtm = createdDtm;
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

    public boolean getIsBrowser() {
        return isBrowser;
    }

    public int getFileStatus() {
        return fileStatus;
    }

    public String getModifiedDtm() {
        return modifiedDtm;
    }

    public String getCreatedDtm() {
        return createdDtm;
    }


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileVersion(String fileVersion) {
        this.fileVersion = fileVersion;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public void setIsBrowser(boolean isBrowser) {
        this.isBrowser = isBrowser;
    }

    public void setFileStatus(int fileStatus) {
        this.fileStatus = fileStatus;
    }

    public void setModifiedDtm(String modifiedDtm) {
        this.modifiedDtm = modifiedDtm;
    }

    public void setCreatedDtm(String createdDtm) {
        this.createdDtm = createdDtm;
    }


    @Override
    public String toString() {
        return "FileDataModel{" +
                "fileName='" + fileName + '\'' +
                ", fileVersion='" + fileVersion + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", isBrowser='" + isBrowser + '\'' +
                ", fileStatus=" + fileStatus +
                ", modifiedDtm='" + modifiedDtm + '\'' +
                ", createdDtm='" + createdDtm + '\'' +
                '}';
    }
}
