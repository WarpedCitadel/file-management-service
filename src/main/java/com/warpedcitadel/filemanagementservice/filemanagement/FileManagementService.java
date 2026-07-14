package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.filemanagement.dto.FileDataDto;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileRequestModel;
import org.springframework.stereotype.Service;

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
}
