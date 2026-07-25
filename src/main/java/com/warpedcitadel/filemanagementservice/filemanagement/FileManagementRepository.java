package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.enums.FileStatus;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileModel;
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


    public List<FileModel> updateFileStatus(List<FileModel> files) {
        String updateSQL = loadSQL.loadSQL("/management/update--update_file_status.sql");

        List<FileModel> fileResults = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS)) {

            List<Object> fileUUID = new ArrayList<>();
            List<Object> gameProfileUUID = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                fileUUID.add(files.get(i).getFileUUID());
                gameProfileUUID.add(files.get(i).getGameProfileUUID());
            }
            Object[] fileObjectArray = fileUUID.toArray(new Object[0]);
            Object[] gameProfileObjectArray = gameProfileUUID.toArray(new Object[0]);
            Array fileArray = connection.createArrayOf("uuid", fileObjectArray);
            Array gameProfileArray = connection.createArrayOf("uuid", gameProfileObjectArray);

            updateStatement.setObject(1, gameProfileArray);
            updateStatement.setObject(2, fileArray);
            updateStatement.setInt(3, files.getFirst().getGameStatus());
            ResultSet resultSet = updateStatement.executeQuery();

            int total = 0;
            while (resultSet.next()) {
                String name = resultSet.getString("file_name");
                UUID fileID = (UUID) resultSet.getObject("file_uuid");
                int status = resultSet.getInt("status_type_id");
                UUID gameProfileID = (UUID) resultSet.getObject("game_profile_uuid");
                int platformID = resultSet.getInt("platform_id");
                log.info("Updated file: ({}) file ID: ({}) to ({}) for game profile ID: ({})",
                        name, fileID, FileStatus.getStatusByID(status), gameProfileID);
                FileModel file = new FileModel(
                        gameProfileID,
                        fileID,
                        name,
                        status,
                        platformID
                );
                fileResults.add(file);
                total++;
            }
            log.info("Successfully updated a total ({}) files statuses to ({})",
                    total, FileStatus.getStatusByID(files.getFirst().getGameStatus()));
            return fileResults;
        } catch (SQLException exception) {
            log.error("Failed to update files status to ({}), Reason ({})",
                    FileStatus.getStatusByID(files.getFirst().getGameStatus()), exception.toString());
            throw new RuntimeException("Failed to update files statuses");
        }
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

