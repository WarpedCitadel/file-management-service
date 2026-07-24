package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.enums.FileStatus;
import com.warpedcitadel.filemanagementservice.enums.PlatformOS;
import com.warpedcitadel.filemanagementservice.filemanagement.dto.*;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.SearchAttributesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class FileManagementService {

    private final FileManagementRepository fileManagementRepository;
    private static final Logger log = LoggerFactory.getLogger(FileManagementService.class);

    public FileManagementService(FileManagementRepository fileManagementRepository) {
        this.fileManagementRepository = fileManagementRepository;
    }


    public FileDataDto getFiles(Pageable pageable, SearchAttributesDto attributesDto) {
        int offSet = pageable.getPageNumber() * pageable.getPageSize();
        int limit = pageable.getPageSize();
        long start = System.currentTimeMillis();
        if (limit >= 51) {
            log.warn("Request ({}) files but the page limit is ({})", limit, 51);
            throw new IllegalArgumentException("Content requested too large");
        }
        SearchAttributesModel attributesModel = new SearchAttributesModel();
        List<Object> attributesList = new ArrayList<>();
        if (attributesDto.title() != null &&
                !attributesDto.title().isEmpty()) {
            attributesModel.setTitle(attributesDto.title().concat("%"));
            attributesList.add(attributesModel.getTitle());
        } else {
            attributesModel.setTitle("%");
            attributesList.add(attributesModel.getTitle());
        }

        attributesModel.setGameProfileUUID(attributesDto.gameProfileUUID());
        attributesList.add(attributesModel.getGameProfileUUID());

        attributesModel.setStatusType(attributesDto.statusType());
        attributesList.add(attributesModel.getStatusType());

        attributesModel.setPlatformOS(attributesDto.platformOS());
        attributesList.add(attributesModel.getPlatformOS());

        attributesList.add(limit + 1);
        attributesList.add(offSet);

        List<FileDataModel> fileList = fileManagementRepository.getFiles(attributesList);
        boolean hasNext = fileList.size() > pageable.getPageSize();
        if (hasNext) {
            fileList.remove(fileList.size() - 1);
        }
        SliceImpl<FileDataModel> paginatedList = new SliceImpl<>(fileList, pageable, hasNext);
        SlicedResponse<FileDataModel> filterData = new SlicedResponse<>(paginatedList);
        long elapsed = System.currentTimeMillis() - start;
        log.info("Successfully retrieved ({}) files using query parameters" +
                        " Title: ({}) Game profile ID: ({}) Operating System: ({}) File Status: ({}) with Limit: ({}), Page: ({}) in ({}) ms",
                fileList.size(), attributesDto.title(), attributesDto.gameProfileUUID(),
                PlatformOS.getPlatformByID(attributesDto.platformOS()), FileStatus.getStatusByID(attributesDto.statusType()),
                limit, pageable.getPageNumber(), elapsed);
        return new FileDataDto(filterData);
    }


    public void updateFileStatus(List<FileDto> fileList){
        int limit = fileList.size();
        List<FileModel> files = new ArrayList<>(limit);
        for (int i = 0; limit > i; i++) {
            FileModel fileModel = new FileModel(
                    fileList.get(i).gameProfileUUID(),
                    fileList.get(i).fileUUID(),
                    fileList.get(i).fileStatus()
            );
            files.add(fileModel);
        }
        fileManagementRepository.updateFileStatus(files);
    }


    public StatusTypesDto getStatusTypes() {
        HashMap<Integer, String> statusTypes = fileManagementRepository.getStatusTypes();
        return new StatusTypesDto(
                statusTypes
        );
    }
}
