package com.warpedcitadel.filemanagementservice.fileupload;


import com.warpedcitadel.filemanagementservice.fileupload.model.FileMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageMetaDataModel;
import com.warpedcitadel.filemanagementservice.util.SQLFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileUploadRepository {

    @Autowired
    private DataSource wcDatabase;

    SQLFileReader loadSQL = new SQLFileReader();


    public String recordFileMetaData(FileMetaDataModel file) {

        String insertSQL = loadSQL.loadSQL("/filedata/insert--record_filemetadata.sql");

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

    public ImageMetaDataModel recordImageMetaData(ImageMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/filedata/update--update_user_profile_img.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setString(1, file.getAppUserUUID());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileSize());

            ResultSet resultSet = insertStatement.executeQuery();

            if (resultSet.next()) {

                 file.setFileName(resultSet.getString("file_name"));
                 file.setFileUUID(resultSet.getString("img_uuid"));

                return file;
            } else {

                throw new RuntimeException("Failed to insert file metadata to the database");
            }
        } catch (SQLException exception) {

            throw new RuntimeException("Could not retrieve image UUID: ", exception);
        }
    }


    public List<String> recordGameImageMetaData (List<ImageMetaDataModel> files) {

        String insertSQL = loadSQL.loadSQL("/filedata/insert--record_game_image_file.sql");
        List<String> gameImageList = new ArrayList<>();

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            String[] fileNames = new String[files.size()];
            String[] fileSizes = new String[files.size()];
            Object[] isCover = new Object[files.size()];

            for (int i = 0; i < files.size(); i++) {
                ImageMetaDataModel file = files.get(i);
                fileNames[i] = file.getFileName();
                fileSizes[i] = file.getFileSize();
                isCover[i] = file.getIsCover();
            }

            insertStatement.setString(1, files.getFirst().getAppUserUUID());
            insertStatement.setArray(2, connection.createArrayOf("text", fileNames));
            insertStatement.setArray(3, connection.createArrayOf("text", fileSizes));
            insertStatement.setArray(4, connection.createArrayOf("bool", isCover));

            try (ResultSet rs = insertStatement.executeQuery()) {
                while (rs.next()) {

                    gameImageList.add(rs.getString("file_name"));
                }
            }

        } catch (SQLException exception) {

            throw new RuntimeException("Could not record image metadata", exception);
        }

        return gameImageList;
    }
}