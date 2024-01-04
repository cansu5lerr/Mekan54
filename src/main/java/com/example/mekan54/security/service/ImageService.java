package com.example.mekan54.security.service;

import com.example.mekan54.model.Image;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import com.example.mekan54.repository.ImageRepository;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.nio.file.Paths;
import java.util.*;

import static com.example.mekan54.config.Constants.*;

@Service
public class ImageService {
  @Autowired
    ImageRepository imageRepository;
  @Autowired
    UserDetailsServiceImpl userDetailsService;
  @Autowired
    VenueRepository venueRepository;
  @Autowired
    UserRepository userRepository;

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
                Image image = new Image();
              User user = authenticatedUser;
              image.setUser(user);
              image.setImgUrl(fileUrl);
              imageRepository.save(image);
              userRepository.save(user);
            } else if (objectName.startsWith("venue-")) {
                Venue venue = authenticatedUser.getVenue();
                if (venue != null) {
                    Image image = new Image();
                    List<Image> existingImages = venue.getImages();

                    if (existingImages == null) {
                        existingImages = new ArrayList<>();
                    }

                    image.setImgUrl(fileUrl);
                    existingImages.add(image);
                    venue.setImages(existingImages);
                    image.setVenue(venue);
                    imageRepository.save(image);
                    venueRepository.save(venue);
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Dosya başarıyla yüklendi. Dosya URL: " + fileUrl);
        }

        return ResponseEntity.badRequest().body("Dosya kaydedilemedi.");
    }

    public ResponseEntity<?> updateImage(String token, Long imageId, MultipartFile multipartFile) throws IOException {

        User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
        if(authenticatedUser instanceof User) {
            Optional<Image> imageOptional = imageRepository.findById(imageId);
            if(imageOptional.isPresent()) {
                Image image = imageOptional.get();
                String objectName = "venue-" + authenticatedUser.getVenue().getId()
                        + "-" + image.getId();
                //resmi sil.
                BlobId oldImageBlobId = BlobId.of(FIREBASE_BUCKET, objectName);
                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(FIREBASE_SDK_JSON)))
                        .setProjectId(FIREBASE_PROJECT_ID)
                        .build()
                        .getService();
                storage.delete(oldImageBlobId);
                imageRepository.delete(image);
                //resmi yükle
                FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
                File file = convertMultiPartToFile(multipartFile);
                Path filePath = file.toPath();
                Storage newStorage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(FIREBASE_PROJECT_ID)
                        .build()
                        .getService();
                BlobId newBlobId = BlobId.of(FIREBASE_BUCKET, objectName);
                BlobInfo newBlobInfo = BlobInfo.newBuilder(newBlobId)
                        .setContentType(multipartFile.getContentType())
                        .build();
                newStorage.create(newBlobInfo, Files.readAllBytes(filePath));
                Image newImage = new Image();
                newImage.setImgUrl(getFileUrl(objectName));
                Venue venue = authenticatedUser.getVenue();
                List<Image> existingImages =  venue.getImages();
                existingImages.add(newImage);
                venue.setImages(existingImages);
                venueRepository.save(venue);
                imageRepository.save(newImage);
            }
        }


        return null;
    }

    public ResponseEntity<?> uploadUserImage(String token, MultipartFile multipartFile) throws IOException {
        String objectName = "user-" + userDetailsService.getAuthenticatedUserFromToken(token).getId();
        return uploadImage(token, multipartFile, objectName);
    }

    public ResponseEntity<?> uploadVenueImage(String token, MultipartFile multipartFile) throws IOException {
        String objectName = "venue-" + UUID.randomUUID().toString() + "-" + multipartFile.getOriginalFilename();
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
