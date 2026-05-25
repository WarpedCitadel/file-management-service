package com.warpedcitadel.fileuploadservice.validation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileValidation
{
    /*
    isValidFile checks two properties regarding the uploaded file. 1. If the file is of a correct type. 2. If the file is of appropriate size.
    Parameter: file - the multipart game file
    Return: A bool that represents if the file can be uploaded to the cloud service
     */
    public Boolean isValidFile(MultipartFile file, String[] applicableTypes)
    {
        //Check just to ensure there is a file
        if (file == null || !checkFileSize(file)) return false;

        //Check to see if the file's type is one of the applicable types
        for (String applicableType : applicableTypes) {
            String fileType = getFileExtension(file.getOriginalFilename());
            System.out.println("filetype:" + fileType);
            System.out.println("type:" + applicableType);
            if (fileType.equals(applicableType)) {
                return true;
            }
        }
        return false;
    }

    /*
    checkFileSize checks the file size, ensuring correct size.
     */
    public Boolean checkFileSize(MultipartFile file)
    {
        return file.getSize() < 1000000000;
    }

    /*
    getFileExtension gets the file extension of the provided filename.
    Parameter: fileName - the name of the file as a String
    Return: A String that is the file extension.
     */
    public String getFileExtension(String fileName)
    {
        //Get the last instance of a . in the filename
        int lastDotIndex = fileName.lastIndexOf(".");

        // -1 means there was no period
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1)
        {
           return fileName.substring(lastDotIndex, fileName.length());
        }
        return "";
    }
}
