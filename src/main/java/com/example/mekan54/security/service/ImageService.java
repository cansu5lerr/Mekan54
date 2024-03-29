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
import jakarta.transaction.Transactional;
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
    @Value("${spring.servlet.multipart.max-file-size}") // application.properties veya application.yml'den okunan max dosya boyutu
    private long maxFileSize;
    @Transactional
    public ResponseEntity<?> uploadImages(String token, List<MultipartFile> multipartFiles, List<String> objectNames) throws IOException {
        User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);

        if (authenticatedUser != null) {
            if(!authenticatedUser.getVenue().getImages().isEmpty()) {
                deleteImage(token);
            }
            for (int i = 0; i < multipartFiles.size(); i++) {
                MultipartFile multipartFile = multipartFiles.get(i);
                String objectName = objectNames.get(i);
                if (multipartFile.getSize() > maxFileSize) {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("message", "Dosya boyutu sınırları aşıyor.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMap);
                }
                FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
                File file = convertMultiPartToFile(multipartFile);
                Path filePath = file.toPath();
                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(FIREBASE_PROJECT_ID)
                        .build()
                        .getService();
                BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
                storage.create(blobInfo, Files.readAllBytes(filePath));
                String fileUrl = getFileUrl(objectName);

                if (objectName.startsWith("user-")) {
                    Image image = new Image();
                    image.setImgUrl(fileUrl);
                    image.setUser(authenticatedUser);
                    imageRepository.save(image);
                    authenticatedUser.setProfileImage(image);
                    userRepository.save(authenticatedUser);
                } else if (objectName.startsWith("venue-")) {
                    Venue venue = authenticatedUser.getVenue();
                    if (venue != null) {
                        Image image = new Image();
                        List<Image> existingImages = new ArrayList<>();
                            image.setImgUrl(fileUrl);
                            existingImages.add(image);
                            venue.setImages(existingImages);
                            image.setVenue(venue);
                            imageRepository.save(image);
                            venueRepository.save(venue);

                    }
                }
            }

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Resimler başarıyla yüklendi.");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Dosya kaydedilemedi.");
        return ResponseEntity.badRequest().body(responseMap);
    }

@Transactional
    private ResponseEntity<?> uploadImage(String token, MultipartFile multipartFile, String objectName) throws IOException {
        User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
        if (authenticatedUser != null) {

            if((authenticatedUser.getProfileImage()) != null) {

                deleteImageUser(token);
            }

            FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
            File file = convertMultiPartToFile(multipartFile);
            Path filePath = file.toPath();
            Storage storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .build()
                    .getService();
            BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
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
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Dosya kaydedildi");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message",  "Dosya kaydedilemedi.");
        return ResponseEntity.badRequest().body(responseMap);
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

    public ResponseEntity<?>   uploadVenueImage(String token, List<MultipartFile> multipartFiles) throws IOException {
        List<String> objectNames = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            String objectName = "venue-" + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            objectNames.add(objectName);
        }
        return uploadImages(token, multipartFiles, objectNames);
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
@Transactional
    public boolean  deleteImage(String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if (user instanceof User) {
            Venue venue = user.getVenue();
            List<Image> imageVenue = venue.getImages();
            try {
                for (Image image : imageVenue) {
                    imageRepository.delete(image);
                }
                imageRepository.deleteImagesByVenueId(venue.getId());
                List<Image> imageUrl = new ArrayList<>();
                venue.setImages(imageUrl);
                venueRepository.save(venue);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean deleteImageUser(String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            imageRepository.delete(user.getProfileImage());
            imageRepository.deleteImagesByUserId(user.getId());
            userRepository.save(user);
            return true;
        }
        return false;
    }


}
