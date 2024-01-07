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
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.example.mekan54.config.Constants.*;

@Service
public class FirebaseService {

    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    VenueRepository venueRepository;

    @Autowired
    UserRepository userRepository;
    private static final Logger LOGGER = Logger.getLogger(VenueService.class.getName());


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
        LOGGER.log(Level.INFO, "uploadImageVenue metodu, token: {0} ile çağrıldı.", token);

        User authenticatedUser  = userDetailsService.getAuthenticatedUserFromToken(token);
     if(authenticatedUser instanceof User) {
         String objectName = authenticatedUser.getVenue() != null ?
                 authenticatedUser.getVenue().getId() +"-" +authenticatedUser.getVenue().getVenueName()
                 : "";
         LOGGER.log(Level.INFO, "Dosya adı oluşturuluyor: {0}", objectName);
         String firebaseSdkJsonPath = FIREBASE_SDK_JSON;

         FileInputStream serviceAccount = new FileInputStream(FIREBASE_SDK_JSON);
         LOGGER.info("Firebase SDK JSON dosyası (" + firebaseSdkJsonPath + ") başarıyla okundu.");

         File file = convertMultiPartToFile(multipartFile);
         Path filePath = file.toPath();
         LOGGER.info("Multipart dosyası (" + file.getName() + ") başarıyla dönüştürüldü ve dosya yolu elde edildi: " + filePath);

         Storage storage = StorageOptions.newBuilder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setProjectId(FIREBASE_PROJECT_ID).build().getService();
         LOGGER.info("Storage servisi başarıyla oluşturuldu.");

         BlobId blobId = BlobId.of(FIREBASE_BUCKET, objectName);
         BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(multipartFile.getContentType()).build();
         LOGGER.info("BlobId (" + blobId + ") ve BlobInfo başarıyla oluşturuldu.");

         storage.create(blobInfo, Files.readAllBytes(filePath));
         String fileUrl = getFileUrl(objectName); // Burada objectName, dosyanın adını temsil eder
         LOGGER.log(Level.INFO, "Dosya başarıyla yüklendi. Dosya URL: {0}", fileUrl);

         Venue venue = authenticatedUser.getVenue();
        // venue.setImgUrl(fileUrl);
         venueRepository.save(venue);
         Map<String, String> responseMap = new HashMap<>();
         responseMap.put("message", fileUrl);
         return ResponseEntity.status(HttpStatus.CREATED).body(fileUrl);

     }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message",  "Dosya kaydedilemedi.");
        return ResponseEntity.badRequest().body(responseMap);
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
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/png").build();
            storage.create(blobInfo, Files.readAllBytes(filePath));
            String fileUrl = getFileUrl(objectName);
            if (objectName.startsWith("user-")) {
                User user = authenticatedUser;
                Image userImg= new Image();
                userImg.setUser(user);
                userImg.setImgUrl(fileUrl);
                imageRepository.save(userImg);
                userRepository.save(user);
            } else if (objectName.startsWith("venue-")) {
                Venue venue = authenticatedUser.getVenue();
                if (venue != null) {
                    Image venueImage = new Image();
                    venueImage.setImgUrl(fileUrl);
                    venueImage.setVenue(venue);
                    List<Image> oldImages = venue.getImages();
                    oldImages.add(venueImage);
                    venue.setImages(oldImages);
                    imageRepository.save(venueImage);
                    venueRepository.save(venue);
                }
            }

         // Burada objectName, dosyanın adını temsil eder
            LOGGER.log(Level.INFO, "Dosya başarıyla yüklendi. Dosya URL: {0}", fileUrl);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", fileUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(fileUrl);
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message",  "Dosya kaydedilemedi.");
        return ResponseEntity.badRequest().body(responseMap);
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
