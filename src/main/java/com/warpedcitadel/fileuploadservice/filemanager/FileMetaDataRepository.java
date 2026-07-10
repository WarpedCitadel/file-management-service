package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.filemanager.model.FileMetaDataModel;
import com.warpedcitadel.fileuploadservice.filemanager.model.ImageMetaDataModel;
import com.warpedcitadel.fileuploadservice.util.SQLFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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


    public List<String> recordGameImageMetaData (List<ImageMetaDataModel> files) {

        String insertSQL = loadSQL.loadSQL("/filedata/insert--record_game_image_file.sql");
        List<String> gameImageUUIDList = new ArrayList<>();

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(insertSQL)) {

            String[] fileNames = new String[files.size()];
            String[] fileSizes = new String[files.size()];

            for (int i = 0; i < files.size(); i++) {
                ImageMetaDataModel file = files.get(i);
                fileNames[i] = file.getFileName();
                fileSizes[i] = file.getFileSize();
            }

            stmt.setString(1, files.getFirst().getAppUserUUID()); // All Game profile UUIDS are the same
            stmt.setArray(2, connection.createArrayOf("text", fileNames));
            stmt.setArray(3, connection.createArrayOf("text", fileSizes));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {

                    gameImageUUIDList.add(rs.getString("img_uuid"));
                }
            }

        } catch (SQLException exception) {

            throw new RuntimeException("Could not record image metadata", exception);
        }

        return gameImageUUIDList;
    }
}