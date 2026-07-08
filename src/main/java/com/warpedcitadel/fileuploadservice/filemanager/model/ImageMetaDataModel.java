package com.warpedcitadel.fileuploadservice.filemanager.model;

public class ImageMetaDataModel {

    private String fileUUID;
    private String appUserUUID;
    private String fileName;
    private String fileSize;


    public ImageMetaDataModel() {

    }

    public ImageMetaDataModel(String fileUUID) {
        this.fileUUID= fileUUID;
    }

    public ImageMetaDataModel(String appUserUUID, String fileName, String fileSize) {
        this.appUserUUID = appUserUUID;
        this.fileName = fileName;
        this.fileSize = fileSize;
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


    @Override
    public String toString() {
        return "ImageMetaDataModel{" +
                "fileUUID='" + fileUUID + '\'' +
                ", appUserUUID='" + appUserUUID + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                '}';
    }
}
