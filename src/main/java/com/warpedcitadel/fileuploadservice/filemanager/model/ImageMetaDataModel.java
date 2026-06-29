package com.warpedcitadel.fileuploadservice.filemanager.model;

public class ImageMetaDataModel
{
    private String fileUuid;
    private long appUserId;
    private String appUserUuid;
    private String fileName;
    private String fileSize;

    //Empty Constructor
    public ImageMetaDataModel()
    {

    }

    //File Uuid Only Constructor
    public ImageMetaDataModel(String fileUuid)
    {
        this.fileUuid = fileUuid;
    }

    //Full Constructor
    public ImageMetaDataModel(long appUserId, String fileName, String fileSize)
    {
        this.appUserId = appUserId;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileUuid()
    {
        return fileUuid;
    }

    public long getAppUserId()
    {
        return appUserId;
    }

    public String getAppUserUuid()
    {
        return appUserUuid;
    }

    public String getFileName()
    {
        return fileName;
    }

    public String getFileSize()
    {
        return fileSize;
    }

    @Override
    public String toString() {
        return "FileMetaDataModel{" +
                ", fileUuid='" + fileUuid + '\'' +
                ", appUserId=" + appUserId +
                ", fileName='" + fileName + '\'' +
                ", fileSize='" + fileSize + '\'' +
                '}';
    }
}
