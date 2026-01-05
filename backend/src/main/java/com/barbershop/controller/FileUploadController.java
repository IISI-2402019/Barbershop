package com.barbershop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename with UUID only (to avoid encoding issues with Chinese characters)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            
            Path path = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), path);

            // Return the URL
            // We return the relative path. The frontend should prepend the base URL if needed, 
            // or if it's on the same domain, it works directly.
            // Since frontend is on 5173 and backend on 8080, we need the full URL or the proxy to handle it.
            // But usually we store the relative path in DB.
            // Let's return the relative path "/uploads/filename".
            // The frontend will display it as "http://localhost:8080/uploads/filename".
            
            String fileUrl = "/uploads/" + fileName;
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
