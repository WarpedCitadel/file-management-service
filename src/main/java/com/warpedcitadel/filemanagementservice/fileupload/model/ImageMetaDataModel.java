package com.warpedcitadel.filemanagementservice.fileupload.model;

public class ImageMetaDataModel {

    private String fileUUID;
    private String appUserUUID;
    private String fileName;
    private String fileSize;
    private boolean isCover;


    public ImageMetaDataModel() {

    }

    public ImageMetaDataModel(String appUserUUID, String fileName, String fileSize) {
        this.appUserUUID = appUserUUID;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public ImageMetaDataModel(String appUserUUID, String fileName, String fileSize, boolean isCover) {
        this.appUserUUID = appUserUUID;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.isCover = isCover;
    }


    public String getFileUUID() {
        return fileUUID;
    }

    public String getAppUserUUID() {
        return appUserUUID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public boolean getIsCover() {
        return isCover;
    }

    public void setFileUUID(String fileUUID) {
        this.fileUUID = fileUUID;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "ImageMetaDataModel{" +
                "fileUUID='" + fileUUID + '\'' +
                ", appUserUUID='" + appUserUUID + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", isCover=" + isCover +
                '}';
    }
}