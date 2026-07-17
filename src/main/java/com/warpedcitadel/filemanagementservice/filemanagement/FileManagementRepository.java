package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileRequestModel;
import com.warpedcitadel.filemanagementservice.util.SQLFileReader;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class FileManagementRepository {

    private final DataSource wcDatabase;

    SQLFileReader loadSQL = new SQLFileReader();

    public FileManagementRepository(DataSource wcDatabase) {
        this.wcDatabase = wcDatabase;
    }


    public List<FileDataModel> getGameFiles(FileRequestModel fileRequestModel) {

        String selectSQL = loadSQL.loadSQL("/filedata/select--get_file_list.sql");

        List<FileDataModel> fileList = new ArrayList<>();

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL)) {

            selectStatement.setString(1, fileRequestModel.getGameProfileUUID());

            ResultSet resultSet = selectStatement.executeQuery();

            while (resultSet.next()) {

                FileDataModel file = new FileDataModel();

                file.setFileName(resultSet.getString("file_name"));
                file.setFileVersion(resultSet.getString("file_version"));
                file.setFileSize(resultSet.getString("file_size"));
                file.setIsBrowser(resultSet.getBoolean("isbrowser"));
                file.setFileStatus(resultSet.getInt("status_type_id"));
                file.setModifiedDtm(resultSet.getString("modified_dtm"));
                file.setCreatedDtm(resultSet.getString("created_dtm"));

                fileList.add(file);
            }
        } catch (SQLException exception) {

            throw new RuntimeException("Could not find files for game profile ID: " + fileRequestModel.getGameProfileUUID());
        }

        return fileList;
    }

    
    public int updateFileStatus(FileRequestModel fileRequestModel) {

        String updateSQL = loadSQL.loadSQL("/filedata/update--update_file_status.sql");

        int fileStatus = -1;

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS)) {

            updateStatement.setString(1, fileRequestModel.getGameProfileUUID());
            updateStatement.setInt(2, fileRequestModel.getGameStatus());

            ResultSet resultSet = updateStatement.executeQuery();

            if (resultSet.next()) {

               fileStatus = resultSet.getInt("status_type_id");
            }
        } catch (SQLException exception) {

            throw new RuntimeException("Could not find file for game profile ID: " + fileRequestModel.getGameProfileUUID());
        }

        return fileStatus;
    }


    public HashMap<Integer, String> getStatusTypes() {

        String selectSql = loadSQL.loadSQL("/filedata/select--select_file_status_types.sql");

        HashMap<Integer, String> statusTypes = new HashMap<>(6);

        try (Connection connection = wcDatabase.getConnection();
             Statement selectStatement = connection.createStatement();
             ResultSet resultset = selectStatement.executeQuery(selectSql)) {

            while (resultset.next()) {

                int id = resultset.getInt("id");
                String statusName = resultset.getString("status_type_name");

                statusTypes.put(id, statusName);
            }
        } catch (SQLException exception) {

            throw new RuntimeException("Failed to retrieve a list of status types");
        }

        return statusTypes;
    }
}

