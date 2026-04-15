package com.londres.drogueria.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String baseUploadDir = System.getProperty("user.dir") + "/uploads/";

    public FileStorageService() {
        try {
            Files.createDirectories(Paths.get(baseUploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize base storage directory", e);
        }
    }

    public String storeFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) return null;
        
        try {
            String targetDir = baseUploadDir + subDir + "/";
            Files.createDirectories(Paths.get(targetDir));
            
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(targetDir + fileName);
            Files.copy(file.getInputStream(), path);
            
            return "/uploads/" + subDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error storing file in " + subDir, e);
        }
    }


    public String storeFile(MultipartFile file) {
        return storeFile(file, "productos");
    }
}
