package com.barbershop.controller;

import com.barbershop.model.StoredFile;
import com.barbershop.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private StoredFileRepository storedFileRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            byte[] bytes = file.getBytes();

            StoredFile storedFile = new StoredFile(fileName, contentType, bytes);
            storedFileRepository.save(storedFile);

            // Return the URL for viewing the image
            // We use the ID to retrieve it
            String fileUrl = "/api/upload/image/" + storedFile.getId();
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/image/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        Optional<StoredFile> fileOptional = storedFileRepository.findById(id);
        
        if (fileOptional.isPresent()) {
            StoredFile file = fileOptional.get();
            
            // Guess content type if null
            String contentType = file.getContentType();
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .body(file.getData());
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
