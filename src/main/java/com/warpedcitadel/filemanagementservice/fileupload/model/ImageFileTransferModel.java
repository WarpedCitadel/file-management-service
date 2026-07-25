package com.warpedcitadel.filemanagementservice.fileupload.model;

import java.util.UUID;

public class ImageFileTransferModel {

    private String oldFileName;
    private UUID oldFileUUID;
    private String newFileName;
    private UUID newFileUUID;

    public ImageFileTransferModel() {

    }

    public ImageFileTransferModel(String oldFileName, UUID oldFileUUID,
                                  String newFileName, UUID newFileUUID) {

        this.oldFileName = oldFileName;
        this.oldFileUUID = oldFileUUID;
        this.newFileName = newFileName;
        this.newFileUUID = newFileUUID;
    }


    public String getOldFileName() {
        return oldFileName;
    }

    public UUID getOldFileUUID() {
        return oldFileUUID;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public UUID getNewFileUUID() {
        return newFileUUID;
    }


    public void setOldFileName(String oldFileName) {
        this.oldFileName = oldFileName;
    }

    public void setOldFileUUID(UUID oldFileUUID) {
        this.oldFileUUID = oldFileUUID;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public void setNewFileUUID(UUID newFileUUID) {
        this.newFileUUID = newFileUUID;
    }
}
