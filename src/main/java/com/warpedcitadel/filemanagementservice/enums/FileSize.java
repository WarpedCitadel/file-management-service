package com.warpedcitadel.filemanagementservice.enums;

public enum FileSize {

    FILE_SIZE(1000000000L),
    IMAGE_SIZE(2000000L);

    private final long byteLimit;

    FileSize(long byteLimit) {
        this.byteLimit = byteLimit;
    }

    public long getByteLimit() {
        return byteLimit;
    }
}
