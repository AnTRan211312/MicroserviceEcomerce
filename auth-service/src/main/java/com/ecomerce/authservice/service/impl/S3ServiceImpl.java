package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.advice.exeption.S3UploadException;
import com.ecomerce.authservice.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;
    private final String awsBucketName;
    private final String awsRegion;
    @Override
    public String uploadFile(MultipartFile file, String folder, String fileName, boolean getUrl) {

        try{
            if(file == null || file.isEmpty()){
                throw new S3UploadException("tệp không được rỗng hoặc null");
            }
            String key = String.format("%s/%s", folder, fileName);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsBucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            if (getUrl)
                return String.format("https://%s.s3.%s.amazonaws.com/%s", awsBucketName, awsRegion, key);
            else return key;
        } catch (IOException e) {
            throw new S3UploadException("Lỗi khi cung cấp dữ liệu");
        } catch (Exception e) {
            throw new S3UploadException("Lỗi khi upload file lên S3");
        }
    }

//    @Override
//    public String uploadFile(MultipartFile file, String key, boolean getUrl) {
//        try {
//            if (file == null || file.isEmpty())
//                throw new S3UploadException("T?p logo kh�ng ???c r?ng ho?c null");
//
//            PutObjectRequest putRequest = PutObjectRequest.builder()
//                    .bucket(awsBucketName)
//                    .key(key)
//                    .contentType(file.getContentType())
//                    .build();
//
//            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
//
//            if (getUrl)
//                return String.format("https://%s.s3.%s.amazonaws.com/%s", awsBucketName, awsRegion, key);
//            else return key;
//        } catch (IOException e) {
//            throw new S3UploadException("L?i khi ??c d? li?u t? t?p logo");
//        } catch (Exception e) {
//            throw new S3UploadException("L?i khi upload file l�n S3");
//        }
//    }
//
//    @Override
//    @Cacheable(
//            cacheNames = "presign",
//            key = "#key + ':' + #expireDuration.seconds"
//    )
//    public String generatePresignedUrl(String key, Duration expireDuration) {
//        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//                .bucket(awsBucketName)
//                .key(key)
//                .build();
//
//        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
//                .getObjectRequest(getObjectRequest)
//                .signatureDuration(expireDuration)
//                .build();
//
//        URL presignedUrl = s3Presigner.presignGetObject(presignRequest).url();
//        return presignedUrl.toString();
//    }
//
//    @Override
//    public void deleteFileByUrl(String fileUrl) {
//        try {
//            if (fileUrl == null || fileUrl.isBlank()) {
//                return;
//            }
//
//            String objectKey = extractObjectKeyFromUrl(fileUrl);
//
//            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
//                    .bucket(awsBucketName)
//                    .key(objectKey)
//                    .build();
//
//            s3Client.deleteObject(deleteRequest);
//
//        } catch (Exception e) {
//            throw new S3UploadException("L?i khi x�a file kh?i S3");
//        }
//
//    }
//
//    @Override
//    public void deleteFileByKey(String key) {
//        try {
//            if (key == null || key.isBlank()) {
//                return;
//            }
//
//            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
//                    .bucket(awsBucketName)
//                    .key(key)
//                    .build();
//
//            s3Client.deleteObject(deleteRequest);
//
//        } catch (Exception e) {
//            throw new S3UploadException("L?i khi x�a file kh?i S3");
//        }
//
//    }
//    private String extractObjectKeyFromUrl(String url) {
//        String base = String.format("https://%s.s3.%s.amazonaws.com/", awsBucketName, awsRegion);
//        if (!url.startsWith(base)) {
//            throw new S3UploadException("URL không l? ho?c kh�ng thu?c bucket hi?n t?i");
//        }
//        return url.substring(base.length());
//    }
}
