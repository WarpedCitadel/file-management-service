package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.filemanager.model.FileMetaDataModel;
import com.warpedcitadel.fileuploadservice.filemanager.model.ImageMetaDataModel;
import com.warpedcitadel.fileuploadservice.util.SQLFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class FileMetaDataRepository {

    @Autowired
    private DataSource wcDatabase;

    SQLFileReader loadSQL = new SQLFileReader();


    public String recordFileMetaData(FileMetaDataModel file) {

        String insertSQL = loadSQL.loadSQL("/filedata/insert--record-filemetadata.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            insertStatement.setString(1, file.getGameProfileUUID());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileVersion());
            insertStatement.setString(4, file.getFileSize());

            ResultSet resultSet = insertStatement.executeQuery();

            if (resultSet.next()) {

                String fileUUID = resultSet.getString("file_uuid");
                return fileUUID;
            } else {

                throw new RuntimeException("Failed to insert file metadata to the database");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not find game profile with the UUID: " + file.getGameProfileUUID());
        }
    }

    public String recordImageMetaData(ImageMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/filedata/update--update_user_profile_img.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setString(1, file.getAppUserUUID());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileSize());

            ResultSet resultSet = insertStatement.executeQuery();

            if (resultSet.next()) {

                String fileUUID = resultSet.getString(1);
                return fileUUID;
            } else {

                throw new RuntimeException("Failed to insert file metadata to the database");
            }
        } catch (SQLException exception) {

            throw new RuntimeException("Could not retrieve image UUID: ", exception);
        }
    }
}
