package com.codingshuttle.hackathon.skillsyncai.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String uploadFile(MultipartFile file, String fileName) throws IOException;

    Resource downloadFile(String fileName) throws IOException;

    void deleteFile(String fileName) throws IOException;

    String getFileUrl(String fileName);
}
