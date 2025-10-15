package com.example.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api")
public class UploadController {

    @PostMapping("/uploads/{type}/{folder}")
    public ResponseEntity<String> uploadFile(
            @PathVariable String type,
            @PathVariable String folder,
            @RequestParam("file") MultipartFile file) {
        if (type.equalsIgnoreCase("images")) {
            try {
                // Lưu file vào thư mục uploads
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get("uploads/" + folder + "/" + fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());

                return ResponseEntity.ok(fileName);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed!");
            }
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed!");
        }

    }

}
