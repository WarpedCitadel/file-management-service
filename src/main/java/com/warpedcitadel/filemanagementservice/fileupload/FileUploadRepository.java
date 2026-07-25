package com.warpedcitadel.filemanagementservice.fileupload;


import com.warpedcitadel.filemanagementservice.enums.FileStatus;
import com.warpedcitadel.filemanagementservice.fileupload.model.FileMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageFileTransferModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageMetaDataModel;
import com.warpedcitadel.filemanagementservice.util.SQLFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FileUploadRepository {

    private static final Logger log = LoggerFactory.getLogger(FileUploadRepository.class);
    private final DataSource wcDatabase;

    SQLFileReader loadSQL = new SQLFileReader();

    public FileUploadRepository(DataSource wcDatabase) {
        this.wcDatabase = wcDatabase;
    }


    public void recordFileMetaData(List<UUID> fileUUIDList) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_filemetadata.sql");
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            Object[] fileObjectArray = fileUUIDList.toArray(new Object[0]);
            Array fileArray = connection.createArrayOf("uuid", fileObjectArray);

            insertStatement.setObject(1, fileArray);
            ResultSet resultSet = insertStatement.executeQuery();

            int total = 0;
            while (resultSet.next()) {
                String fileName = resultSet.getString("file_name");
                log.info("Transferring file ({}) to ({})",
                        fileName, FileStatus.DEPLOY.getDescription());
                total++;
            }
            if (fileUUIDList.size() != total) {
                log.error("Requested a transfer total of ({}) files, but only transferred ({})",
                        fileUUIDList.size(), total);
                throw new RuntimeException("Failed to transfer all files");
            }
            log.info("A total of ({}) files was transferred", total);
        } catch (SQLException exception) {
            log.error("Failed to record game files Reason: ({})", exception.toString());
            throw new RuntimeException("Failed to transfer game files");
        }
    }


    public List<String> recordFileToStaging(List<FileMetaDataModel> files) {
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
            insertStatement.setObject(1, files.getFirst().getGameProfileUUID());
            insertStatement.setArray(2, connection.createArrayOf("text", fileNames));
            insertStatement.setArray(3, connection.createArrayOf("text", fileSizes));
            insertStatement.setArray(4, connection.createArrayOf("smallint", osPlatforms));
            try (ResultSet resultSet = insertStatement.executeQuery()) {
                while (resultSet.next()) {
                    gameFileList.add(resultSet.getString("file_name"));
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to record ({}) game files to staging for game profile ID: ({})  Reason: ({})",
                    files.size(), files.getFirst().getGameProfileUUID(), exception.toString());
            throw new RuntimeException("Failed to record game files to staging");
        }
        return gameFileList;
    }


    public void deleteStagingGameFiles(UUID gameProfileUUID) {
        String deleteSQL = loadSQL.loadSQL("/upload/delete--delete_staging_game_file_record.sql");
        List<String> imageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL, Statement.RETURN_GENERATED_KEYS)) {
            deleteStatement.setObject(1, gameProfileUUID);
            ResultSet resultSet = deleteStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                imageList.add(resultSet.getString("file_name"));
                log.warn("Deleting file ({}) from game profile ID: ({})", imageList.get(i), gameProfileUUID);
                i++;
            }
        } catch (SQLException exception) {
            log.error("Failed to remove game files from staging for game profile ID: ({}) Reason: ({})",
                    gameProfileUUID, exception.toString());
            throw new RuntimeException("Failed to remove game files");
        }
    }


    public void updateFileStatus(UUID gameProfileUUID, int status) {
        String updateSQL = loadSQL.loadSQL("/upload/update--update_file_status.sql");
        List<String> imageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS)) {
            updateStatement.setObject(1, gameProfileUUID);
            updateStatement.setInt(2, status);
            ResultSet resultSet = updateStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                imageList.add(resultSet.getString("file_name"));
                log.info("Updating file ({}) to ({}) for game profile ID: ({})",
                        imageList.get(i), FileStatus.getStatusByID(status), gameProfileUUID);
                i++;
            }
        } catch (SQLException exception) {
            log.error("Failed updating files to status to ({}) for game profile ID: ({}) Reason: ({})",
                    FileStatus.getStatusByID(status), gameProfileUUID, exception.toString());
            throw new RuntimeException("Failed to update file status");
        }
    }


    public void recordGameImageMetaData(UUID gameProfileUUID) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_game_image_file.sql");
        List<String> gameImageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
            insertStatement.setObject(1, gameProfileUUID);
            try (ResultSet resultSet = insertStatement.executeQuery()) {
                while (resultSet.next()) {
                    gameImageList.add(resultSet.getString("file_name"));
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to record game images for game profile ID: ({}) Reason: ({})",
                    gameProfileUUID, exception.toString());
            throw new RuntimeException("Failed to record game images");
        }
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
            insertStatement.setObject(1, files.getFirst().getAppUserUUID());
            insertStatement.setArray(2, connection.createArrayOf("text", fileNames));
            insertStatement.setArray(3, connection.createArrayOf("text", fileSizes));
            insertStatement.setArray(4, connection.createArrayOf("bool", isCover));

            try (ResultSet resultSet = insertStatement.executeQuery()) {
                while (resultSet.next()) {
                    gameImageList.add(resultSet.getString("file_name"));
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to record ({}) game images to staging for game profile ID: ({})  Reason: ({})",
                    files.size(), files.getFirst().getAppUserUUID(), exception.toString());
            throw new RuntimeException("Failed to record game images to staging");
        }
        return gameImageList;
    }


    public void deleteStagingGameImages(UUID gameProfileUUID) {
        String deleteSQL = loadSQL.loadSQL("/upload/delete--delete_staging_game_image_record.sql");
        List<String> imageList = new ArrayList<>();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL, Statement.RETURN_GENERATED_KEYS)) {
            deleteStatement.setObject(1, gameProfileUUID);
            ResultSet resultSet = deleteStatement.executeQuery();
            int i = 0;
            while (resultSet.next()) {
                imageList.add(resultSet.getString("file_name"));
                log.warn("Deleting game images ({}) from game profile ID: ({})",
                        imageList.get(i), gameProfileUUID);
                i++;
            }
        } catch (SQLException exception) {
            log.error("Failed to remove game images from staging for game profile ID: ({}) Reason: ({})",
                    gameProfileUUID, exception.toString());
            throw new RuntimeException("Failed removing games images from staging");
        }
    }


    public ImageFileTransferModel recordImageMetaData(ImageMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/upload/update--update_user_profile_img.sql");
        ImageFileTransferModel profileImage = new ImageFileTransferModel();
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setObject(1, file.getAppUserUUID());
            ResultSet resultSet = insertStatement.executeQuery();
            if (resultSet.next()) {
                 profileImage.setNewFileName(resultSet.getString("new_file_name"));
                 profileImage.setNewFileUUID((UUID) resultSet.getObject("new_img_uuid"));
                 profileImage.setOldFileName(resultSet.getString("old_file_name"));
                 profileImage.setOldFileUUID((UUID) resultSet.getObject("old_img_uuid"));
            }
        } catch (SQLException exception) {
            log.error("Failed to record profile image to staging for user profile ID: ({}) Reason: ({})",
                    file.getAppUserUUID(), exception.toString());
            throw new RuntimeException("Failed to record profile image");
        }
        return profileImage;
    }


    public ImageMetaDataModel recordImageMetaDataToStaging(ImageMetaDataModel file) {
        String insertSQL = loadSQL.loadSQL("/upload/insert--record_image_metadata.sql");
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setObject(1, file.getAppUserUUID());
            insertStatement.setString(2, file.getFileName());
            insertStatement.setString(3, file.getFileSize());
            ResultSet resultSet = insertStatement.executeQuery();
            if (resultSet.next()) {
                file.setFileName(resultSet.getString("file_name"));
                file.setFileUUID((UUID) resultSet.getObject("img_uuid"));
            }
        } catch (SQLException exception) {
            log.error("Failed to record profile image for user profile ID: ({}) Reason: ({})",
                    file.getAppUserUUID(), exception.toString());
            throw new RuntimeException("Failed to record profile image to staging");
        }
        return file;
    }


    public void deleteStagingImage(ImageMetaDataModel file) {
        String deleteSQL = loadSQL.loadSQL("/upload/delete--delete_staging_image_record.sql");
        try (Connection connection = wcDatabase.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL, Statement.RETURN_GENERATED_KEYS)) {
            deleteStatement.setObject(1, file.getAppUserUUID());
            ResultSet resultSet = deleteStatement.executeQuery();
            if (resultSet.next()) {
                file.setFileName(resultSet.getString("file_name"));
                file.setFileUUID((UUID) resultSet.getObject("img_uuid"));
                log.warn("Deleting profile image ({}), file ID: ({}) from user profile ID: ({})",
                        file.getFileName(), file.getFileUUID(), file.getAppUserUUID());
            }
        } catch (SQLException exception) {
            log.error("Failed to remove profile image from staging for user profile ID: ({}) Reason: ({})",
                    file.getAppUserUUID(), exception.toString());
            throw new RuntimeException("Failed to remove profile image from staging");
        }
    }
}