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


    public String recordFileMetaData(FileMetaDataModel file){

        String insertSQL = loadSQL.loadSQL("/filemetadata/insert--record-filemetadata.sql");

        try (Connection connection = wcDatabase.getConnection();
        PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setLong(1, file.getAppUserId());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileVersion());
            insertStatement.setString(4, file.getFileSize());

            int rowAffected = insertStatement.executeUpdate();

            if (rowAffected == 1){
                try (ResultSet resultSet = insertStatement.getGeneratedKeys()) {
                    if (resultSet.next()) return resultSet.getString(2);
                    System.out.println(resultSet.getInt(2));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to insert file metadata to the database", exception);
        }
        throw new RuntimeException("Failed to retrieve object file UUID");
    }

//    #### HELPER FUNCTIONS ####

    public long getUserByUuid(String uuid){

        String selectSQL = loadSQL.loadSQL("/users/select--get_app_user_id.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL)) {

            selectStatement.setString(1, uuid);

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (SQLException exception){
            throw new RuntimeException("User with the uuid: " + uuid + " does not exist", exception);
        }
        throw new RuntimeException("User id does not exist");
    }
}
