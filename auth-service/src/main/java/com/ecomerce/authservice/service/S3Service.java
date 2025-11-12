package com.ecomerce.authservice.service;

import org.springframework.web.multipart.MultipartFile;


public interface S3Service {
    String uploadFile(MultipartFile file,String folder,String fileName,boolean getUrl);

//    String uploadFile(MultipartFile file, String key, boolean getUrl);
//
//    String generatePresignedUrl(String key, Duration expireDuration);
//
//    void deleteFileByUrl(String fileUrl);
//
//    void deleteFileByKey(String key);
}
