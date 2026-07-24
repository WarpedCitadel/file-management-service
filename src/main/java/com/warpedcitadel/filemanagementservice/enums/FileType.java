package com.warpedcitadel.filemanagementservice.enums;

public enum FileType {

    FILE_TYPE(new String[]{".zip"}),
    IMAGE_TYPE(new String[]{".jpeg", ".png", ".jpg"});

    private final String[] fileType;

    FileType(String[] fileType) {
        this.fileType = fileType;
    }

    public String[] getAcceptedFileTypes() {
        return fileType;
    }
}
