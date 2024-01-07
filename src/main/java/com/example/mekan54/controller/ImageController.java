package com.example.mekan54.controller;

import com.example.mekan54.security.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class ImageController {
    @Autowired
    ImageService imageService;

    @PostMapping("/addImageVenue")
    public ResponseEntity<?> addImageVenue(@RequestHeader("Authorization") String token, @RequestParam("file") List<MultipartFile> file) throws IOException {
        return imageService.uploadVenueImage(token,file);
    }
    @PostMapping("/addImageUser")
    public ResponseEntity<?> addImageUser(@RequestHeader("Authorization") String token, @RequestParam("file")MultipartFile file) throws IOException {
        return imageService.uploadUserImage(token,file);
    }

}
