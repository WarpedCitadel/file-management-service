package com.warpedcitadel.filemanagementservice.fileupload;


import com.warpedcitadel.filemanagementservice.filetransfer.FileTransferService;
import com.warpedcitadel.filemanagementservice.filetransfer.dto.RequestData;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameFileDetails;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameFileDto;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDetails;
import com.warpedcitadel.filemanagementservice.fileupload.dto.GameImageDto;
import com.warpedcitadel.filemanagementservice.enums.FileSize;
import com.warpedcitadel.filemanagementservice.enums.FileType;
import com.warpedcitadel.filemanagementservice.fileupload.model.FileMetaDataModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageFileTransferModel;
import com.warpedcitadel.filemanagementservice.fileupload.model.ImageMetaDataModel;
import com.warpedcitadel.filemanagementservice.enums.FileStatus;
import com.warpedcitadel.filemanagementservice.fileupload.validation.FileValidation;
import com.warpedcitadel.filemanagementservice.util.VirusScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${aws.valid-bucket.name}")
    private String validName;
    @Value("${aws.image-bucket.name}")
    private String imageBucketName;

    private final S3Client s3Client;
    private final FileUploadRepository repository;
    private final FileValidation fileValidation;
    private final VirusScanService clamAVClient;
    private final FileTransferService fileTransferService;
    private final Logger log = LoggerFactory.getLogger(FileUploadService.class);


    public FileUploadService(S3Client s3Client, FileUploadRepository repository,
                             FileValidation fileValidation, VirusScanService clamAVClient,
                             FileTransferService fileTransferService) {
        this.s3Client = s3Client;
        this.repository = repository;
        this.fileValidation = fileValidation;
        this.clamAVClient = clamAVClient;
        this.fileTransferService = fileTransferService;
    }


    public void uploadFilesToS3(List<MultipartFile> files, List<GameFileDetails> fileDetails) throws IOException {
        try {
            long start = System.currentTimeMillis();
            if (files.size() == fileDetails.size()) {
                fileValidation.checkFilePlatformOS(fileDetails);
            } else {
                log.error("A total of ({}) files was uploaded but the total affiliated metadata for each file was ({})",
                        files.size(), fileDetails.size());
                throw new IllegalArgumentException(
                        "Each file must have corresponding file details.");
            }
            List<GameFileDto> fileMetaData = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                if (!fileValidation.isValidFile(files.get(i),
                        FileType.FILE_TYPE.getAcceptedFileTypes(), FileSize.FILE_SIZE.getByteLimit())) {
                    throw new IllegalArgumentException("Invalid file type: " + files.get(i).getContentType() +
                            " for file " + files.get(i).getOriginalFilename() + ". Please provide a " +
                            Arrays.toString(FileType.FILE_TYPE.getAcceptedFileTypes()) + " file or reduce file byte size");
                }
                fileMetaData.add(new GameFileDto(files.get(i), fileDetails.get(i)));
            }
            List<String> fileNames = recordFilesToStaging(fileMetaData);
            List<String> prefixList = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                String prefix = "games/" + fileDetails.get(i).gameProfileUUID() + "/files/" + fileNames.get(i);
                uploadFileS3(files.get(i), prefix);
                prefixList.add(prefix);
                boolean result = clamAVClient.processFile(files.get(i));
                if (!result) {
                    for (int j = 0; prefixList.size() > j; j++) {
                        deleteS3Objects(validName, prefixList.get(j));
                    }
                    repository.deleteStagingGameFiles(fileDetails.getFirst().gameProfileUUID());
                    throw new IOException("Malformed file detected");
                }
            }
            if (files.size() == fileNames.size()) {
                long elapsed = System.currentTimeMillis() - start;
                log.info("A total of ({}) files scanned successfully for game profile ID: ({}) in ({}) ms",
                        fileNames.size() , fileDetails.getFirst().gameProfileUUID(), elapsed);
                repository.updateFileStatus(fileDetails.getFirst().gameProfileUUID(), FileStatus.REVIEW.getCode());
            }
        } catch(IOException exception) {
            log.error("Failed to upload game files for game profile ID: ({}), Reason: ({})",
                    fileDetails.getFirst().gameProfileUUID(), exception.toString());
            throw exception;
        }
    }


    private List<String> recordFilesToStaging(List<GameFileDto> gameFileDto) {
        List<FileMetaDataModel> gameFileModelList = new ArrayList<>();
        for (int i = 0; gameFileDto.size() > i; i++) {
            String fileSize = formatBytes(gameFileDto.get(i).file());
            FileMetaDataModel fileDetails = new FileMetaDataModel(
                    gameFileDto.get(i).fileDetails().gameProfileUUID(),
                    gameFileDto.get(i).file().getOriginalFilename(),
                    gameFileDto.get(i).fileDetails().platformOS(),
                    fileSize
            );
            gameFileModelList.add(fileDetails);
        }
        return repository.recordFileToStaging(gameFileModelList);
    }


    private void uploadFileS3(MultipartFile file, String key) throws IOException {
        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(validName)
                            .key(key)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));
            s3Client.utilities().getUrl(builder -> builder.bucket(validName)
                    .key(key)).toExternalForm();
        } catch (IOException exception) {
            log.error("Failed uploading file to ({}) bucket using object key ({})", validName, key);
            throw new IOException("Failed uploading files to storage");
        }
    }


    public void uploadGameImageToS3(List<MultipartFile> files, List<GameImageDetails> fileDetails) throws IOException {
        try {
            long start = System.currentTimeMillis();
            if (files.size() == fileDetails.size()) {
                fileValidation.checkCoverImageCount(fileDetails);
            } else {
                log.error("A total of ({}) images was uploaded but the total affiliated metadata for each image was ({})",
                        files.size(), fileDetails.size());
                throw new IllegalArgumentException(
                        "Each file must have corresponding file details.");
            }
            List<GameImageDto> imageMetaData = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                if (!fileValidation.isValidFile(files.get(i),
                        FileType.IMAGE_TYPE.getAcceptedFileTypes(), FileSize.IMAGE_SIZE.getByteLimit())) {
                    throw new IllegalArgumentException("Invalid image type: " + files.get(i).getContentType() +
                            " for file " + files.get(i).getOriginalFilename() +
                            ". Please provide a " + Arrays.toString(FileType.IMAGE_TYPE.getAcceptedFileTypes()) + " file or reduce file byte size");
                }
                imageMetaData.add(new GameImageDto(files.get(i), fileDetails.get(i)));
            }
            List<String> fileNames = recordGameImageToStaging(imageMetaData);
            List<String> prefixList = new ArrayList<>();
            List<RequestData> gameImages = new ArrayList<>();
            for (int i = 0; files.size() > i; i++) {
                String prefix = "images/games/" + fileDetails.get(i).gameProfileUUID() + "/gameImages/" + fileNames.get(i);
                uploadFileS3(files.get(i), prefix);
                RequestData image = new RequestData(
                        fileDetails.get(i).gameProfileUUID(),
                        fileNames.get(i)
                );
                gameImages.add(image);
                prefixList.add(prefix);
                boolean result = clamAVClient.processFile(files.get(i));
                if (!result) {
                    for (int j = 0; prefixList.size() > j; j++) {
                        deleteS3Objects(validName, prefixList.get(j));
                    }
                    repository.deleteStagingGameImages(fileDetails.getFirst().gameProfileUUID());
                    throw new IOException("Malformed file detected");
                }
            }
            if (files.size() == gameImages.size()) {
                repository.recordGameImageMetaData(fileDetails.getFirst().gameProfileUUID());
                fileTransferService.transferGameImagesToS3(gameImages);
                long elapsed = System.currentTimeMillis() - start;
                log.info("A total of ({}) game images files uploaded successfully for game profile ID: ({}) in ({}) ms",
                        gameImages.size(), fileDetails.getFirst().gameProfileUUID(), elapsed);
            }
        } catch (IOException exception) {
            log.error("Failed to upload game images for game profile ID: ({}), Reason: ({})",
                    fileDetails.getFirst().gameProfileUUID(), exception.toString());
            throw exception;
        }
    }


    public List<String> recordGameImageToStaging(List<GameImageDto> gameImageList) {
        List<ImageMetaDataModel> gameImageModelList = new ArrayList<>();
        for (int i = 0; gameImageList.size() > i; i++) {
            String fileSize = formatBytes(gameImageList.get(i).file());
            ImageMetaDataModel imageMetaDataModel = new ImageMetaDataModel(
                    gameImageList.get(i).details().gameProfileUUID(),
                    updateFileName(gameImageList.get(i).file().getOriginalFilename()),
                    fileSize,
                    gameImageList.get(i).details().isCover()
            );
            gameImageModelList.add(imageMetaDataModel);
        }
        return repository.recordGameImagesToStaging(gameImageModelList);
    }


    public void uploadImageToS3(MultipartFile file, ImageMetaDataModel fileDetails) throws IOException {
        try {
            long start = System.currentTimeMillis();
            if (!fileValidation.isValidFile(file,
                    FileType.IMAGE_TYPE.getAcceptedFileTypes(), FileSize.IMAGE_SIZE.getByteLimit())) {
                throw new IllegalArgumentException("Invalid content type: " + file.getContentType() +
                        " for file " + file.getOriginalFilename() +
                        ". Please provide a " + Arrays.toString(FileType.IMAGE_TYPE.getAcceptedFileTypes()) +
                        " file or reduce file byte size");
            }
            ImageMetaDataModel image = recordImageMetaDataToStaging(file, fileDetails);
            String prefix = "images/users/" + image.getFileUUID() + "/image/" + image.getFileName();
            uploadFileS3(file, prefix);
            boolean result = clamAVClient.processFile(file);
            if (!result) {
                deleteS3Objects(validName, prefix);
                repository.deleteStagingImage(fileDetails);
                throw new IOException("Malformed file content detected");
            } else {
                ImageFileTransferModel profileImageModel = repository.recordImageMetaData(fileDetails);
                RequestData profileImage = new RequestData(
                        profileImageModel.getNewFileUUID(),
                        profileImageModel.getNewFileName()
                );
                String oldPrefix = "images/users/" + profileImageModel.getOldFileUUID() +
                        "/image/" + profileImageModel.getOldFileName();
                fileTransferService.transferProfileImageToS3(profileImage);
                if (!isStaticImage(profileImageModel.getOldFileUUID())) {
                    deleteS3Objects(imageBucketName, oldPrefix);
                }
                long elapsed = System.currentTimeMillis() - start;
                log.info("Profile image file uploaded successfully for user profile ID: ({}) in ({}) ms",
                        fileDetails.getAppUserUUID(), elapsed);
            }
        } catch (IOException exception) {
            log.error("Failed to upload profile image for user profile ID: ({}), Reason: ({})",
                    fileDetails.getAppUserUUID(), exception.toString());
            throw exception;
        }
    }


    public ImageMetaDataModel recordImageMetaDataToStaging(MultipartFile file, ImageMetaDataModel fileDetails) {
        String fileSize = formatBytes(file);
        ImageMetaDataModel imageMetaData = new ImageMetaDataModel(
                fileDetails.getAppUserUUID(),
                updateFileName(file.getOriginalFilename()),
                fileSize
        );
        return repository.recordImageMetaDataToStaging(imageMetaData);
    }


    private void deleteS3Objects(String bucketName, String prefix) {
        String continuationToken = null;
        do {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build();
            ListObjectsV2Response objectList = s3Client.listObjectsV2(listRequest);
            List<ObjectIdentifier> deletionList = new ArrayList<>();
            for (S3Object s3Object : objectList.contents()) {
                deletionList.add(ObjectIdentifier.builder().key(s3Object.key()).build());
            }
            if (!deletionList.isEmpty()) {
                DeleteObjectsRequest deleteS3Objects = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(builder -> builder.objects(deletionList))
                        .build();
                s3Client.deleteObjects(deleteS3Objects);
            }
            continuationToken = objectList.nextContinuationToken();
        } while (continuationToken != null);
    }


    private String formatBytes(MultipartFile file) {
        long sizeInBytes = file.getSize();
        if (sizeInBytes < 1024) {
            return sizeInBytes + "B";
        } else {
            sizeInBytes = sizeInBytes / 1024;
            if (sizeInBytes < 1024) {
                return sizeInBytes + "KB";
            }
            sizeInBytes = sizeInBytes / 1024;
            return sizeInBytes + "MB";
        }
    }


    private String updateFileName(String rawFileName){
        int lastDotIndex = rawFileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < rawFileName.length() - 1) {
            String newFilename = UUID.randomUUID().toString();
            return newFilename + rawFileName.substring(lastDotIndex);
        }
        return rawFileName;
    }


    private boolean isStaticImage(String fileUUID) {
        String[] staticAssets = new String[]{"019f7b16-635a-7c13-b15d-ed3c75ad61f9"};
        for (String file : staticAssets) {
            if (fileUUID.equals(file)) {
                return true;
            }
        }
        return false;
    }
}