package com.warpedcitadel.filemanagementservice.filemanagement;

import com.warpedcitadel.filemanagementservice.filemanagement.model.FileDataModel;
import com.warpedcitadel.filemanagementservice.filemanagement.model.FileRequestModel;
import com.warpedcitadel.filemanagementservice.util.SQLFileReader;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
                file.setCreatedDtm("created_dtm");

                fileList.add(file);
            }
        } catch (SQLException exception) {

            throw new RuntimeException("Could not find files for game profile ID: " + fileRequestModel.getGameProfileUUID());
        }

        return fileList;
    }
}
