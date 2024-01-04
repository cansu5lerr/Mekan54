package com.example.mekan54.controller;

import com.example.mekan54.security.service.FirebaseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.example.mekan54.config.Constants.*;

@RestController
@RequestMapping("/api/auth")
public class FirebaseController {

    @Autowired
    private FirebaseService firebaseService;

   /* @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        return firebaseService.uploadFile(file);
    } */

    @PostMapping("/uploadImage")
    public ResponseEntity<?> uploadImages(@RequestHeader("Authorization") String token , @RequestParam("file") MultipartFile file) throws  Exception {
        return firebaseService.uploadImageVenue(token,file);
    }

    @GetMapping("/view/{fileName}")
    public ResponseEntity<?> viewFile(@PathVariable String fileName) {
        try {
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(FIREBASE_SDK_JSON)))
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .build()
                    .getService();
            BlobId blobId = BlobId.of(FIREBASE_BUCKET, fileName);
            Blob blob = storage.get(blobId);
            byte[] inputStream = blob.getContent();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(blob.getContentType()))
                    .body(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}