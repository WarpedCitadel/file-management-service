package com.warpedcitadel.fileuploadservice.filemanager;


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

        String insertSQL = loadSQL.loadSQL("/filemetadata/insert--record-filemetadata.sql");

        try (Connection connection = wcDatabase.getConnection();
        PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            insertStatement.setString(1, file.getGameProfileUUID());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileVersion());
            insertStatement.setString(4, file.getFileSize());

            ResultSet resultSet = insertStatement.executeQuery();

            if (resultSet.next()){
                String fileUUID = resultSet.getString("file_uuid");
                return fileUUID;
            } else {
                throw new RuntimeException("Failed to insert file metadata to the database");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not find game profile with the UUID: " + file.getGameProfileUUID());
        }
    }

    public String recordImageMetaData(ImageMetaDataModel file) throws SQLException
    {
        String insertSQL = loadSQL.loadSQL("/filemetadata/update--app_user_profile_img_update.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

                 insertStatement.setString(1, file.getFileName());
                 insertStatement.setString(2, file.getFileSize());
                 insertStatement.setLong(3, file.getAppUserId());

                 int rowAffected = insertStatement.executeUpdate();

                 if (rowAffected == 1) {
                     try (ResultSet resultSet = insertStatement.getGeneratedKeys()) {
                         if (resultSet.next()) return resultSet.getString(2);
                     }
                 }

        } catch (SQLException exception)
        {
            throw new RuntimeException("Failed to insert image metadata into the database", exception);
        }
        throw new SQLException("Failed to retrieve object file UUID");
    }

//    #### HELPER FUNCTIONS ####

    public long getUserByUuid(String uuid) {

        String selectSQL = loadSQL.loadSQL("/users/select--get_app_user_id.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL)) {

            selectStatement.setString(1, uuid);

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong("id");
            } else {
                throw new RuntimeException("User with the uuid: " + uuid + " does not exist");
            }
        } catch (SQLException exception){
            throw new RuntimeException("Failed to retrieve user data");
        }
    }
}
