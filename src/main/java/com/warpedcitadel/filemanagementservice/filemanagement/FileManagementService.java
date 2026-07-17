package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.filemanagement.dto.FileDataDto;
import com.warpedcitadel.filemanagementservice.filemanagement.dto.FileRequestDto;
import com.warpedcitadel.filemanagementservice.filemanagement.dto.StatusTypesDto;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileRequestModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class FileManagementService {

    private final FileManagementRepository fileManagementRepository;


    public FileManagementService(FileManagementRepository fileManagementRepository) {
        this.fileManagementRepository = fileManagementRepository;
    }


    public FileDataDto getGameFiles(String gameProfileUUID) {

        FileRequestModel fileRequestModel = new FileRequestModel(
                gameProfileUUID
        );

        List<FileDataModel> fileList = fileManagementRepository.getGameFiles(fileRequestModel);

        return new FileDataDto(fileList);
    }


    public FileRequestDto updateFileStatus(FileRequestDto fileRequestDto){

        FileRequestModel fileRequestModel = new FileRequestModel(
                fileRequestDto.gameProfileUUID(),
                fileRequestDto.fileStatus()
        );

        int fileStatus = fileManagementRepository.updateFileStatus(fileRequestModel);

        if (fileStatus == -1) {
            throw new RuntimeException("Failed to update profile status");
        }

        FileRequestDto fileRequest = new FileRequestDto(fileRequestDto.gameProfileUUID(), fileStatus);
        return fileRequest;
    }


    protected StatusTypesDto getStatusTypes() {

        HashMap<Integer, String> statusTypes = fileManagementRepository.getStatusTypes();

        return new StatusTypesDto(
                statusTypes
        );
    }
}
