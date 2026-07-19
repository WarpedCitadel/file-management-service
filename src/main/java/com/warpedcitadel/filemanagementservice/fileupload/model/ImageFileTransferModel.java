package com.warpedcitadel.filemanagementservice.fileupload.model;

public class ImageFileTransferModel {

    private String oldFileName;
    private String oldFileUUID;
    private String newFileName;
    private String newFileUUID;

    public ImageFileTransferModel() {

    }

    public ImageFileTransferModel(String oldFileName, String oldFileUUID,
                                  String newFileName, String newFileUUID) {

        this.oldFileName = oldFileName;
        this.oldFileUUID = oldFileUUID;
        this.newFileName = newFileName;
        this.newFileUUID = newFileUUID;
    }


    public String getOldFileName() {
        return oldFileName;
    }

    public String getOldFileUUID() {
        return oldFileUUID;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public String getNewFileUUID() {
        return newFileUUID;
    }


    public void setOldFileName(String oldFileName) {
        this.oldFileName = oldFileName;
    }

    public void setOldFileUUID(String oldFileUUID) {
        this.oldFileUUID = oldFileUUID;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public void setNewFileUUID(String newFileUUID) {
        this.newFileUUID = newFileUUID;
    }
}
