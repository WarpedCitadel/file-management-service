package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.enums.FileStatus;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileRequestModel;
import com.warpedcitadel.filemanagementservice.util.SQLFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Repository
public class FileManagementRepository {

    private final DataSource database;
    private static final Logger log = LoggerFactory.getLogger(FileManagementRepository.class);
    SQLFileReader loadSQL = new SQLFileReader();

    public FileManagementRepository(DataSource database) {
        this.database = database;
    }


    public List<FileDataModel> getFiles(List<Object> attributesList) {
        String selectSQL = loadSQL.loadSQL("/management/select--get_file_list.sql");
        List<FileDataModel> fileList = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL)) {
            int request;
            for (request = 0; attributesList.size() > request; request++) {
                if (attributesList.get(request) != null && !attributesList.get(request).equals(-1)) {
                    selectStatement.setObject(request + 1, attributesList.get(request));
                } else {
                    selectStatement.setObject(request + 1, null);
                }
            }
            ResultSet resultSet = selectStatement.executeQuery();
            while (resultSet.next()) {
                FileDataModel fileDataModel = new FileDataModel(
                        resultSet.getLong("app_user_id"),
                        (UUID) resultSet.getObject("game_profile_uuid"),
                        resultSet.getString("title"),
                        (UUID) resultSet.getObject("file_uuid"),
                        resultSet.getString("file_name"),
                        resultSet.getString("file_size"),
                        resultSet.getInt("platform_id"),
                        resultSet.getInt("status_type_id"),
                        resultSet.getString("created_dtm")
                );
                fileList.add(fileDataModel);
            }
        } catch (SQLException exception) {
            log.error("Failed to retrieve list of files Reason: ({})", exception.toString());
            throw new RuntimeException("Failed to retrieve list of files");
        }
        return fileList;
    }


    public int updateFileStatus(FileRequestModel fileRequestModel) {
        String updateSQL = loadSQL.loadSQL("/management/update--update_file_status.sql");
        int fileStatus = -1;
        try (Connection connection = database.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS)) {
            updateStatement.setString(1, fileRequestModel.getGameProfileUUID());
            updateStatement.setInt(2, fileRequestModel.getGameStatus());
            ResultSet resultSet = updateStatement.executeQuery();
            if (resultSet.next()) {
               fileStatus = resultSet.getInt("status_type_id");
            }
        } catch (SQLException exception) {
            log.error("Failed to update file status to ({}) for game profile ID: ({})",
                    FileStatus.getStatusByID(fileRequestModel.getGameStatus()), fileRequestModel.getGameProfileUUID());
            throw new RuntimeException("Failed to update file status");
        }
        return fileStatus;
    }


    public HashMap<Integer, String> getStatusTypes() {
        String selectSql = loadSQL.loadSQL("/management/select--select_file_status_types.sql");
        HashMap<Integer, String> statusTypes = new HashMap<>(FileStatus.values().length);
        try (Connection connection = database.getConnection();
             Statement selectStatement = connection.createStatement();
             ResultSet resultset = selectStatement.executeQuery(selectSql)) {
            while (resultset.next()) {
                int id = resultset.getInt("id");
                String statusName = resultset.getString("status_type_name");
                statusTypes.put(id, statusName);
            }
        } catch (SQLException exception) {
            log.error("Failed to retrieve list of status types Reason: ({})",
                    exception.toString());
            throw new RuntimeException("Failed to retrieve list of status types");
        }
        return statusTypes;
    }
}

