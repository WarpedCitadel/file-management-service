package com.warpedcitadel.filemanagementservice.filemanagement.model;

public class FileRequestModel {

    String gameProfileUUID;
    int gameStatus;

    public FileRequestModel() {

    }

    public FileRequestModel(String gameProfileUUID) {
        this.gameProfileUUID = gameProfileUUID;
    }

    public FileRequestModel(String gameProfileUUID, int gameStatus) {
        this.gameProfileUUID = gameProfileUUID;
        this.gameStatus = gameStatus;
    }


    public String getGameProfileUUID() {
        return gameProfileUUID;
    }

    public int getGameStatus() {
        return gameStatus;
    }


    public void setGameStatus(int gameStatus) {
        this.gameStatus = gameStatus;
    }
}
