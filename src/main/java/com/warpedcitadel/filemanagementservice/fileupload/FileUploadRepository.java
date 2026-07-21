package com.warpedcitadel.filemanagementservice.fileupload;


import com.warpedcitadel.filemanagementservice.fileupload.model.FileMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageFileTransferModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.status.FileStatus;
import com.warpedcitadel.filemanagementservice.util.SQLFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileUploadRepository {

    private static final Logger log = LoggerFactory.getLogger(FileUploadRepository.class);
    private final DataSource wcDatabase;

    SQLFileReader loadSQL = new SQLFileReader();

    public FileUploadRepository(DataSource wcDatabase) {
        this.wcDatabase = wcDatabase;
    }


    public String recordFileMetaData(FileMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_filemetadata.sql");
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
            insertStatement.setString(1, file.getGameProfileUUID());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setInt(3, file.getPlatformOS());
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


    public List<String> recordFileToStaging( List<FileMetaDataModel> files) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_files_staging.sql");
        List<String> gameFileList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
            String[] fileNames = new String[files.size()];
            String[] fileSizes = new String[files.size()];
            Integer[] osPlatforms = new Integer[files.size()];
            for (int i = 0; i < files.size(); i++) {
                FileMetaDataModel file = files.get(i);
                fileNames[i] = file.getFileName();
                fileSizes[i] = file.getFileSize();
                osPlatforms[i] = file.getPlatformOS();
            }
            insertStatement.setString(1, files.getFirst().getGameProfileUUID());
            insertStatement.setArray(2, connection.createArrayOf("text", fileNames));
            insertStatement.setArray(3, connection.createArrayOf("text", fileSizes));
            insertStatement.setArray(4, connection.createArrayOf("smallint", osPlatforms));
            try (ResultSet resultSet = insertStatement.executeQuery()) {
                while (resultSet.next()) {
                    gameFileList.add(resultSet.getString("file_name"));
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to record ({}) game files to staging for game profile ID: ({}) | Error: {}",
                    files.size(), files.getFirst().getGameProfileUUID(), exception.toString());
            throw new RuntimeException("Failed to record game images");
        }
        return gameFileList;
    }


    public void updateFileStatus(String gameProfileUUID, int status) {
        String updateSQL = loadSQL.loadSQL("/upload/update--update_file_status.sql");
        List<String> imageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS)) {
            updateStatement.setString(1, gameProfileUUID);
            updateStatement.setInt(2, status);
            ResultSet resultSet = updateStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                imageList.add(resultSet.getString("file_name"));
                log.info("Updating file ({}) to ({}) for game profile ID: ({})", imageList.get(i), FileStatus.getStatusByID(status), gameProfileUUID);
                i++;
            }
        } catch (SQLException exception) {
            log.error("Failed updating files to ({}) for game profile ID: ({}) | Error: {}",
                    FileStatus.getStatusByID(status), gameProfileUUID, exception.toString());
            throw new RuntimeException("Failed to update file status");
        }
    }


    public void deleteStagingGameFiles(String gameProfileUUID) {
        String deleteSQL = loadSQL.loadSQL("/upload/delete--delete_staging_game_file_record.sql");
        List<String> imageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL, Statement.RETURN_GENERATED_KEYS)) {
            deleteStatement.setString(1, gameProfileUUID);
            ResultSet resultSet = deleteStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                imageList.add(resultSet.getString("file_name"));
                log.info("Deleting file ({}) from game profile ID: {}", imageList.get(i), gameProfileUUID);
                i++;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not retrieve game profile UUID: " + gameProfileUUID);
        }
    }


    public ImageFileTransferModel recordImageMetaData(ImageMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/upload/update--update_user_profile_img.sql");
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, file.getAppUserUUID());
            ResultSet resultSet = insertStatement.executeQuery();
            ImageFileTransferModel profileImage = new ImageFileTransferModel();
            if (resultSet.next()) {
                 profileImage.setNewFileName(resultSet.getString("new_file_name"));
                 profileImage.setNewFileUUID(resultSet.getString("new_img_uuid"));
                 profileImage.setOldFileName(resultSet.getString("old_file_name"));
                 profileImage.setOldFileUUID(resultSet.getString("old_img_uuid"));
                return profileImage;
            } else {
                throw new RuntimeException("Failed to insert file metadata to the database");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not retrieve image UUID: ", exception);
        }
    }


    public ImageMetaDataModel recordImageMetaDataToStaging(ImageMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_image_metadata.sql");
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


    public ImageMetaDataModel deleteStagingImage(ImageMetaDataModel file) {
        String deleteSQL = loadSQL.loadSQL("/upload/delete--delete_staging_image_record.sql");

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL, Statement.RETURN_GENERATED_KEYS)) {
            deleteStatement.setString(1, file.getAppUserUUID());
            ResultSet resultSet = deleteStatement.executeQuery();

            if (resultSet.next()) {
                file.setFileName(resultSet.getString("file_name"));
                file.setFileUUID(resultSet.getString("img_uuid"));
                log.info("Deleting profile image ({}), file ID: {}", file.getFileName(), file.getFileUUID());
                return file;
            } else {
                throw new RuntimeException("Failed to delete staging image data");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not retrieve image UUID: ", exception);
        }
    }


    public List<String> recordGameImageMetaData(String gameProfileUUID) {

        String insertSQL = loadSQL.loadSQL("/upload/insert--record_game_image_file.sql");
        List<String> gameImageList = new ArrayList<>();

        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            insertStatement.setString(1, gameProfileUUID);

            try (ResultSet resultSet = insertStatement.executeQuery()) {
                while (resultSet.next()) {
                    gameImageList.add(resultSet.getString("file_name"));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not record image metadata", exception);
        }
        return gameImageList;
    }


    public List<String> recordGameImagesToStaging(List<ImageMetaDataModel> files) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_game_images_staging.sql");
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

            try (ResultSet resultSet = insertStatement.executeQuery()) {
                while (resultSet.next()) {
                    gameImageList.add(resultSet.getString("file_name"));
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to record game images to staging, Error: {}", exception.getMessage());
            throw new RuntimeException("Failed to record game images");
        }
        return gameImageList;
    }


    public List<String> deleteStagingGameImages(String gameProfileUUID) {
        String deleteSQL = loadSQL.loadSQL("/upload/delete--delete_staging_game_image_record.sql");
        List<String> imageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL, Statement.RETURN_GENERATED_KEYS)) {
            deleteStatement.setString(1, gameProfileUUID);
            ResultSet resultSet = deleteStatement.executeQuery();

            int i = 0;
            while (resultSet.next()) {
                imageList.add(resultSet.getString("file_name"));
                log.info("Deleting game image ({}) from game profile ID: {}", imageList.get(i), gameProfileUUID);
                i++;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Could not retrieve game profile UUID: ", exception);
        }
        return imageList;
    }
}