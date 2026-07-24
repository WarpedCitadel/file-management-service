package com.warpedcitadel.filemanagementservice.enums;

public enum PlatformOS {

    BROWSER(1, "HTML5"),
    WINDOWS(2, "WINDOWS"),
    LINUX(3, "LINUX"),
    MACOS(4, "MACINTOSH");

    private final int code;
    private final String description;

    PlatformOS(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }


    public static String getPlatformByID(int statusID) {
        for (PlatformOS platform : PlatformOS.values()) {
            if (platform.code == statusID) {
                return platform.description;
            } else {
                if (statusID == -1) {
                    return "null";
                }
            }
        }
        throw new IllegalArgumentException("Unknown code: " + statusID);
    }
}
