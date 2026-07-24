package com.warpedcitadel.filemanagementservice.filemanagement.model;

import java.util.UUID;

public class SearchAttributesModel {

    String title;
    UUID gameProfileUUID;
    Integer statusType;
    Integer platformOS;


    public SearchAttributesModel() {

    }

    public SearchAttributesModel(String title, UUID gameProfileUUID, Integer statusType, Integer platformOS) {
        this.title = title;
        this.gameProfileUUID = gameProfileUUID;
        this.statusType = statusType;
        this.platformOS = platformOS;
    }


    public String getTitle() {
        return title;
    }

    public UUID getGameProfileUUID() {
        return gameProfileUUID;
    }

    public Integer getStatusType() {
        return statusType;
    }

    public Integer getPlatformOS() {
        return platformOS;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setGameProfileUUID(UUID gameProfileUUID) {
        this.gameProfileUUID = gameProfileUUID;
    }

    public void setStatusType(Integer statusType) {
        this.statusType = statusType;
    }

    public void setPlatformOS(Integer platformOS) {
        this.platformOS = platformOS;
    }
}
