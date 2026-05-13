package com.warpedcitadel.fileuploadservice.filemanager;


import com.warpedcitadel.fileuploadservice.util.SQLFileReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class FileRepository {

    @Autowired
    private DataSource wcDatabase;

    SQLFileReader loadSQL = new SQLFileReader();

    public int recordFile(FileModel file){

        String insertSQL = loadSQL.loadSQL("/filemetadata/uploadmetadata.sql");


        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setLong(1, file.getAppUserId());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileURL());
            insertStatement.setString(4, file.getFileVersion());
            insertStatement.setString(5, file.getFileSize());
            insertStatement.setString(6, file.getFileType());

            int rowAffected = insertStatement.executeUpdate();

            if (rowAffected == 1){
                try (ResultSet resultSet = insertStatement.getGeneratedKeys()) {
                    if (resultSet.next()) return resultSet.getInt(1);
                }

            }

            return -1;

        } catch (SQLException genericException) {
            throw new RuntimeException("Database Error", genericException);
        }
    }
}
