package com.warpedcitadel.filemanagementservice.user;

public class AppUserModel {

    private String appUserUuid;
    private long appUserId;
    private String username;


    public AppUserModel(){

    }

    public AppUserModel(String appUserUuid) {
        this.appUserUuid = appUserUuid;
    }


    public String getAppUserUuid() {
        return appUserUuid;
    }


    @Override
    public String toString() {
        return "AppUserModel{" +
                "appUserUuid='" + appUserUuid + '\'' +
                ", appUserId=" + appUserId +
                ", username='" + username + '\'' +
                '}';
    }
}
