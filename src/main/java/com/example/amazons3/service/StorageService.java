package com.example.amazons3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.example.amazons3.payload.Attachment;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    @Autowired
    public StorageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }


    public String uploadFile(MultipartHttpServletRequest request) {

        Iterator<String> fileNames = request.getFileNames();

        while (fileNames.hasNext()) {

            MultipartFile file = request.getFile(fileNames.next());

            Assert.notNull(file, "File must be not null");

            uploadFileToS3(file);

        }

        return "File successful uploaded";

    }

    public ResponseEntity<?> downloadFile(String fileName) throws IOException {

        Attachment attachment = downloadFileFromS3(fileName);

        assert attachment != null;
        byte[] bytes = attachment.getBytes();

        return ResponseEntity
                .ok()
                .contentLength(bytes.length)
                .header("Content-type", attachment.getContentType())
                .header("Content-disposition", "attachment; filename=\"" + fileName + "\"")
                .body(bytes);
    }

    public String deleteFile(String fileName) {

        deleteFileFromS3(fileName);
        return "File successful deleted";

    }

    private void uploadFileToS3(MultipartFile file) {

        File convertMultipartFileToFile = convertMultipartFileToFile(file);

        amazonS3.putObject(
                new PutObjectRequest(
                        bucketName,
                        UUID.randomUUID().toString(),
                        convertMultipartFileToFile
                )
        );

        convertMultipartFileToFile.delete();

    }

    public Attachment downloadFileFromS3(String fileName) {

        S3Object s3Object = amazonS3.getObject(bucketName, fileName);

        S3ObjectInputStream inputStream = s3Object.getObjectContent();

        String contentType = s3Object.getObjectMetadata().getContentType();

        try {

            byte[] bytes = IOUtils.toByteArray(inputStream);

            return new Attachment(
                    bytes,
                    contentType,
                    "name",
                    4096
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public String getValueFromFile(String fileName) throws IOException { //Returns the fields in an excel file copied from AWS
        String path = "";
        S3Object s3Object = amazonS3.getObject(bucketName, fileName);

        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        inputStream.close();
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();

        while (iterator.hasNext()) {
            Row row = iterator.next();

            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();

                if (cell.getCellType()==Cell.CELL_TYPE_NUMERIC){
                    path = path  + cell.getNumericCellValue()+ "=";
                }
            }
        }
        return path;
    }

    private void deleteFileFromS3(String fileName) {

        amazonS3.deleteObject(bucketName, fileName);

    }

    private File convertMultipartFileToFile(MultipartFile file) {

        File convertedFile = new File(file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            // error
            e.printStackTrace();
        }

        return convertedFile;
    }


}
