package com.warpedcitadel.filemanagementservice.enums;

public enum FileStatus {

    NEW(1, "New file status"),
    PROCESSING(2, "Processing file status"),
    REVIEW(3, "Review needed status"),
    READY(4, "Ready to deploy status"),
    DEPLOY(5, "Deploy to production status"),
    DELETED(6, "Deleted file status");


    private final int code;
    private final String description;


    private FileStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }


    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }


    public static String getStatusByID(int statusID) {
        for (FileStatus status : FileStatus.values()) {
            if (status.code == statusID) {
                return status.description;
            } else {
                if (statusID == -1) {
                    return "null";
                }
            }
        }
        throw new IllegalArgumentException("Unknown code: " + statusID);
    }
}
