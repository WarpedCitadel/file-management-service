package com.warpedcitadel.filemanagementservice.filemanagement.model;

import java.util.UUID;

public class FileModel {

    UUID gameProfileUUID;
    UUID fileUUID;
    int gameStatus;

    public FileModel() {

    }


    public FileModel(UUID gameProfileUUID, UUID fileUUID, int gameStatus) {
        this.gameProfileUUID = gameProfileUUID;
        this.fileUUID = fileUUID;
        this.gameStatus = gameStatus;
    }


    public UUID getGameProfileUUID() {
        return gameProfileUUID;
    }

    public int getGameStatus() {
        return gameStatus;
    }

    public UUID getFileUUID() {
        return fileUUID;
    }


    @Override
    public String toString() {
        return "FileModel{" +
                "gameProfileUUID=" + gameProfileUUID +
                ", fileUUID=" + fileUUID +
                ", gameStatus=" + gameStatus +
                '}';
    }
}
