package com.example.mekan54.security.service;

import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

import static com.example.mekan54.config.Constants.*;

@Service
public class FirebaseService {

    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    VenueRepository venueRepository;

    @Autowired
    UserRepository userRepository;


    public ResponseEntity<?> uploadImagUser (String token,MultipartFile multipartFile) throws IOException {
        User authenticatedUser  = userDetailsService.getAuthenticatedUserFromToken(token);
        if(authenticatedUser instanceof User) {
            String objectName = authenticatedUser.getEmail();
            FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
            File file = convertMultiPartToFile(multipartFile);
            Path filePath = file.toPath();

            Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
            BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();

            storage.create(blobInfo, Files.readAllBytes(filePath));
            String fileUrl = getFileUrl(objectName); // Burada objectName, dosyanın adını temsil eder
            Venue venue = authenticatedUser.getVenue();
          //  venue.setImgUrl(fileUrl);
            venueRepository.save(venue);
            return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully. File URL: " + fileUrl);

        }
        return ResponseEntity.badRequest().body("Dosya kaydedilemedi.");
    }

    public ResponseEntity<?> uploadImageVenue (String token,MultipartFile multipartFile) throws IOException {
     User authenticatedUser  = userDetailsService.getAuthenticatedUserFromToken(token);
     if(authenticatedUser instanceof User) {
         String objectName = authenticatedUser.getVenue() != null ?
                 authenticatedUser.getVenue().getId() +"-" +authenticatedUser.getVenue().getVenueName()
                 : "";
         FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
         File file = convertMultiPartToFile(multipartFile);
         Path filePath = file.toPath();
         Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
         BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
         BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();

         storage.create(blobInfo, Files.readAllBytes(filePath));
         String fileUrl = getFileUrl(objectName); // Burada objectName, dosyanın adını temsil eder
         Venue venue = authenticatedUser.getVenue();
        // venue.setImgUrl(fileUrl);
         venueRepository.save(venue);
         return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully. File URL: " + fileUrl);

     }
        return ResponseEntity.badRequest().body("Dosya kaydedilemedi.");
    }

    private ResponseEntity<?> uploadImage(String token, MultipartFile multipartFile, String objectName) throws IOException {
        User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
        if (authenticatedUser != null) {
            FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
            File file = convertMultiPartToFile(multipartFile);
            Path filePath = file.toPath();
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .build()
                    .getService();
            BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(multipartFile.getContentType())
                    .build();
            storage.create(blobInfo, Files.readAllBytes(filePath));
            String fileUrl = getFileUrl(objectName);
            if (objectName.startsWith("user-")) {
                User user = authenticatedUser;

                userRepository.save(user);
            } else if (objectName.startsWith("venue-")) {
                Venue venue = authenticatedUser.getVenue();
                if (venue != null) {

                    venueRepository.save(venue);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Dosya başarıyla yüklendi. Dosya URL: " + fileUrl);
        }

        return ResponseEntity.badRequest().body("Dosya kaydedilemedi.");
    }

    public ResponseEntity<?> uploadUserImage(String token, MultipartFile multipartFile) throws IOException {
        String objectName = "user-" + userDetailsService.getAuthenticatedUserFromToken(token).getId() +"-"+userDetailsService.getAuthenticatedUserFromToken(token).getName();
        return uploadImage(token, multipartFile, objectName);
    }

    public ResponseEntity<?> uploadVenueImage(String token, MultipartFile multipartFile) throws IOException {
        String objectName = "venue-" + userDetailsService.getAuthenticatedUserFromToken(token).getVenue().getId()
               +"-" + userDetailsService.getAuthenticatedUserFromToken(token).getVenue().getVenueName();
        return uploadImage(token, multipartFile, objectName);
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    private String generateFileName(MultipartFile multiPart) {
        return new Date().getTime() + "-" + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
    }

    private String getFileUrl(String objectName) {
        return "https://firebasestorage.googleapis.com/v0/b/" + FIREBASE_BUCKET + "/o/" + objectName + "?alt=media";
    }
}
