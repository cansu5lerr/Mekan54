package com.example.mekan54.security.service;

import com.example.mekan54.model.*;
import com.example.mekan54.payload.request.CategoryNameRequest;
import com.example.mekan54.payload.request.RegisterAdminRequest;
import com.example.mekan54.payload.request.VenueUpdateRequest;
import com.example.mekan54.payload.response.VenueUpdateResponse;
import com.example.mekan54.payload.response.VenueResponse;
import com.example.mekan54.repository.CategoryRepository;
import com.example.mekan54.repository.RoleRepository;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class VenueService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CategoryService categoryService;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    ImageService imageService;
//Venue ekle
    public ResponseEntity<?> registerAdmin(RegisterAdminRequest registerAdminRequest) {

        String[] fieldsToCheck = {"Mekan adı", "Adres", "Email", "Kategori", "Sifre", "SifreTekrarı"};

        for (String field : fieldsToCheck) {
            String value = getValue(registerAdminRequest, field);
            if (value == null || value.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", field + " boş geçilemez.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        String email = registerAdminRequest.getEmail();
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        if (!pattern.matcher(email).matches()) {
            Map<String, String> messageResponse = new HashMap<>();
            messageResponse.put("error","Email formatı uygun değil.");
            return ResponseEntity.badRequest().body(messageResponse);
        }
        if (userRepository.existsByEmail(registerAdminRequest.getEmail())) {
            Map<String, String> messageResponse = new HashMap<>();
            messageResponse.put("error","Bu email adresi alınmıştır.");
            return ResponseEntity.badRequest().body(messageResponse);
        }
        if (!registerAdminRequest.getPassword().equals(registerAdminRequest.getRepeatPassword())) {
            Map<String, String> messageResponse = new HashMap<>();
            messageResponse.put("error","Şifre tekrarında hata var.");
            return ResponseEntity.badRequest().body(messageResponse);
        }
        User user = new User(registerAdminRequest.getEmail(),
                encoder.encode(registerAdminRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);
        Long categoryId = categoryService.getCategoryId(registerAdminRequest.getCategoryName());
        Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            Venue venue = new Venue();
            venue.setUser(user);
            venue.setVenueName(registerAdminRequest.getVenueName());
            venue.setCategory(category);
            venue.setAdress(registerAdminRequest.getAdress());
            List<Venue> categoryVenue = new ArrayList<>();
            categoryVenue.add(venue);
            category.setVenues(categoryVenue);
            user.setVenue(venue);
            venueRepository.save(venue);
            userRepository.save(user);
            categoryRepository.save(category);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Mekan başarılı bir şekilde kaydedildi.");
            return ResponseEntity.ok().body(responseMap);
        }
        Map<String, String> messageResponse = new HashMap<>();
        messageResponse.put("error","Hatalı giriş tekrar deneyin.");
        return ResponseEntity.badRequest().body(messageResponse);
    }
// value getir
    private String getValue(RegisterAdminRequest registerAdminRequest, String field) {
        switch (field) {
            case "Mekan adı":
                return registerAdminRequest.getVenueName();
            case "Adres":
                return registerAdminRequest.getAdress();
            case "Email":
                return registerAdminRequest.getEmail();
            case "Kategori":
                return registerAdminRequest.getCategoryName();
            case "Sifre":
                return registerAdminRequest.getPassword();
            case "SifreTekrarı":
                return registerAdminRequest.getRepeatPassword();
            default:
                throw new IllegalArgumentException("Geçersiz alan adı: " + field);
        }
    }
    //mekanların hepsini listele

    public ResponseEntity<?> getVenues (String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            List<Venue> venueList = venueRepository.findAll();
            List<VenueResponse> venuesResponsesList = new ArrayList<>();
            for (Venue venue : venueList) {
                VenueResponse venuesResponse = new VenueResponse();
                venuesResponse.setVenueName(venue.getVenueName());
                List<String> imageUrls = new ArrayList<>();
                List<Image> images = venue.getImages();
                if (images != null && !images.isEmpty()) {
                    for (Image image : images) {
                        imageUrls.add(image.getImgUrl());
                    }
                }
                venuesResponse.setImgUrl(imageUrls);
                venuesResponse.setAdress(venue.getAdress());
                venuesResponse.setCategoryName(venue.getCategory().getCategoryName());
                venuesResponse.setFavoriteSize(venue.getFavorites().size());
                List<Map<String, String>> commentsResponseList = new ArrayList<>();
                for (Comment comment : venue.getComments()) {
                    Map<String, String> commentMap = new HashMap<>();
                    commentMap.put("name", comment.getUser().getName());
                    commentMap.put("surname", comment.getUser().getSurname());
                    commentMap.put("comment", comment.getContent());
                    String imgUrl = (comment.getUser() != null && comment.getUser().getProfileImage() != null)
                            ? comment.getUser().getProfileImage().getImgUrl()
                            : null;

                    commentMap.put("imgUrl", imgUrl);
                    commentsResponseList.add(commentMap);
                }
                Map<String, String> aboutMap = new HashMap<>();
                aboutMap.put("workingHour", venue.getWorkingHour());
                aboutMap.put("phoneNumber", venue.getPhoneNumber());
                aboutMap.put("address", venue.getAdress());
                aboutMap.put("webSite", venue.getWebsite());
                venuesResponse.setAbout(aboutMap);
                venuesResponse.setComments(commentsResponseList);
                venuesResponsesList.add(venuesResponse);
            }
            return ResponseEntity.ok().body(venuesResponsesList);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı.");
        return ResponseEntity.badRequest().body(responseMap);
    }

    public ResponseEntity <?> getVenuesWithCategories (String token,CategoryNameRequest categoryNameRequest) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            Optional<Category> categoryOptional = categoryRepository.findByCategoryName(categoryNameRequest.getCategoryName());
            List<VenueResponse> venuesResponsesList = new ArrayList<>();
            if (categoryOptional.isPresent()) {
                Category category = categoryOptional.get();
                List<Venue> venues = category.getVenues();
                for (Venue venue : venues) {
                    VenueResponse venuesResponse = new VenueResponse();
                    venuesResponse.setCategoryName(venue.getCategory().getCategoryName());
                    venuesResponse.setVenueName(venue.getVenueName());
                    venuesResponse.setAdress(venue.getAdress());
                    List<String> imageUrls = new ArrayList<>();
                    List<Image> images = venue.getImages();
                    if (images != null && !images.isEmpty()) {
                        for (Image image : images) {
                            imageUrls.add(image.getImgUrl());
                        }
                    }
                    venuesResponse.setImgUrl(imageUrls);
                    venuesResponse.setFavoriteSize(venue.getFavorites().size());

                    List<Map<String, String>> commentsResponseList = new ArrayList<>();
                    for (Comment comment : venue.getComments()) {
                        Map<String, String> commentMap = new HashMap<>();
                        commentMap.put("name", comment.getUser().getName());
                        commentMap.put("surname", comment.getUser().getSurname());
                        commentMap.put("comment", comment.getContent());
                        String imgUrl = (comment.getUser() != null && comment.getUser().getProfileImage() != null)
                                ? comment.getUser().getProfileImage().getImgUrl()
                                : null;

                        commentMap.put("imgUrl", imgUrl);
                        commentsResponseList.add(commentMap);
                    }

                    Map<String, String> aboutMap = new HashMap<>();
                    aboutMap.put("workingHour", venue.getWorkingHour());
                    aboutMap.put("phoneNumber", venue.getPhoneNumber());
                    aboutMap.put("address", venue.getAdress());
                    aboutMap.put("webSite", venue.getWebsite());
                    venuesResponse.setAbout(aboutMap);
                    venuesResponse.setComments(commentsResponseList);
                    venuesResponsesList.add(venuesResponse);
                }
                return ResponseEntity.ok().body(venuesResponsesList);
            } else {
                return ResponseEntity.notFound().build(); // Category not found
            }
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Hatalı giriş.");
        return ResponseEntity.badRequest().body(responseMap);
    }

 /* public ResponseEntity<?> getVenuesByName(VenueNameRequest venueNameRequest) {
      List<Venue> venues = venueRepository.findAllByVenueName(venueNameRequest.getVenueName());

      if (!venues.isEmpty()) {
          List<Map<String, Object>> venuesResponse = new ArrayList<>();

          for (Venue venue : venues) {
              Map<String, Object> venueResponse = new HashMap<>();
              venueResponse.put("venueName", venue.getVenueName());
              List<String> imageUrls = new ArrayList<>();
              List<Image> images = venue.getImages();
              if (images != null && !images.isEmpty()) {
                  for (Image image : images) {
                      imageUrls.add(image.getImgUrl());
                  }
              }
              venueResponse.put("imgrl", imageUrls);
              venueResponse.put("address", venue.getAdress());
              venueResponse.put("category", venue.getCategory().getCategoryName());
              venueResponse.put("favoriteSize", venue.getFavorites().size());
              List<Map<String, String>> commentsResponse = new ArrayList<>();
              for (Comment comment : venue.getComments()) {
                  Map<String, String> commentMap = new HashMap<>();
                  commentMap.put("name", comment.getUser().getName());
                  commentMap.put("surname", comment.getUser().getSurname());
                  commentMap.put("comment", comment.getContent());
                  commentsResponse.add(commentMap);
              }
              venueResponse.put("comments", commentsResponse);

              venuesResponse.add(venueResponse);
          }

          return ResponseEntity.ok().body(venuesResponse);
      } else {
          return ResponseEntity.badRequest().body("Böyle bir mekan adı bulunmamaktadır.");
      }
  }*/

    public ResponseEntity<?> getVenuesByName(String token,String venueName) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof  User) {
        List<Venue> venues = venueRepository.findAllByVenueName(venueName);

        List<VenueResponse> venuesResponseList = new ArrayList<>();

        for (Venue venue : venues) {
            VenueResponse venuesResponse = new VenueResponse();
            venuesResponse.setVenueName(venue.getVenueName());

            List<String> imageUrls = new ArrayList<>();
            List<Image> images = venue.getImages();
            if (images != null && !images.isEmpty()) {
                for (Image image : images) {
                    imageUrls.add(image.getImgUrl());
                }
            }
            venuesResponse.setImgUrl(imageUrls);

            venuesResponse.setAdress(venue.getAdress());
            venuesResponse.setCategoryName(venue.getCategory().getCategoryName());
            venuesResponse.setFavoriteSize(venue.getFavorites().size());

            List<Map<String, String>> commentsResponseList = new ArrayList<>();
            for (Comment comment : venue.getComments()) {
                Map<String, String> commentMap = new HashMap<>();
                commentMap.put("name", comment.getUser().getName());
                commentMap.put("surname", comment.getUser().getSurname());
                commentMap.put("comment", comment.getContent());
                String imgUrl = (comment.getUser() != null && comment.getUser().getProfileImage() != null)
                        ? comment.getUser().getProfileImage().getImgUrl()
                        : null;

                commentMap.put("imgUrl", imgUrl);                commentsResponseList.add(commentMap);
            }

            Map<String, String> aboutMap = new HashMap<>();
            aboutMap.put("workingHour", venue.getWorkingHour());
            aboutMap.put("phoneNumber", venue.getPhoneNumber());
            aboutMap.put("address", venue.getAdress());
            aboutMap.put("webSite", venue.getWebsite());
            venuesResponse.setAbout(aboutMap);
            venuesResponse.setComments(commentsResponseList);

            venuesResponseList.add(venuesResponse);
        }

        return ResponseEntity.ok().body(venuesResponseList);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Hatalı giriş.");
        return ResponseEntity.badRequest().body(responseMap);
    }
     public ResponseEntity<?> updateVenue (String token, VenueUpdateRequest venueRequest) {
      User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
      if(authenticatedUser instanceof User) {
          Optional<Venue> venueOptional = venueRepository.findById(authenticatedUser.getVenue().getId());
          if(venueOptional.isPresent()) {
              Venue venue = venueOptional.get();
              if(!venueRequest.getVenueName().isEmpty()) {
                  venue.setVenueName(venueRequest.getVenueName());
              }
              if(!venueRequest.getAdress().isEmpty()) {
                venue.setAdress(venueRequest.getAdress());
              }
              if(!venueRequest.getPhoneNumber().isEmpty()) {
                  venue.setPhoneNumber(venueRequest.getPhoneNumber());
              }
              if(!venueRequest.getWebsite().isEmpty()) {
                  venue.setPhoneNumber(venueRequest.getWebsite());
              }
              if(!venueRequest.getWorkingHour().isEmpty()){
                  venue.setWorkingHour(venue.getWorkingHour());
              }
              if(!venueRequest.getPhoneNumber().isEmpty()) {
                  venue.setPhoneNumber(venueRequest.getPhoneNumber());
              }
              if(!venueRequest.getCategoryName().isEmpty()) {
                 String categoryName = venueRequest.getCategoryName();
                 Long categoryId = categoryService.getCategoryId(categoryName);
                 Optional<Category> optionalCategory = categoryRepository.findById(categoryId);
                 if(optionalCategory.isPresent()) {
                     Category category = optionalCategory.get();
                     venue.setCategory(category);
                     List<Venue> categoryVenue = new ArrayList<>();
                     categoryVenue.add(venue);
                     category.setVenues(categoryVenue);
                     categoryRepository.save(category);
                 }
              }
              venueRepository.save(venue);
             // return ResponseEntity.ok().body(new VenueUpdateResponse(venue.getVenueName(),venue.getCategory().getCategoryName(), venueRequest.getAdress(), venueRequest.));
              return ResponseEntity.ok().body(new VenueUpdateResponse(
                      venue.getVenueName(),
                      venue.getCategory().getCategoryName(),
                      venue.getPhoneNumber(),
                      venue.getWorkingHour(),
                      venue.getWebsite(),
                      venue.getAdress()
              ));
          }
          return ResponseEntity.badRequest().body("Mekan bulunamadı.");
      }
      return ResponseEntity.badRequest().body("Kullanıcı girişi başarısız.");
     }
     public ResponseEntity<?> getVenueOwner (String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            Venue venue = user.getVenue();
            VenueResponse venueResponse = new VenueResponse();
            venueResponse.setVenueName(venue.getVenueName());
            venueResponse.setCategoryName(venue.getCategory().getCategoryName());
            List<String> imageUrls = new ArrayList<>();
            List<Image> images = venue.getImages();
            if (images != null && !images.isEmpty()) {
                for (Image image : images) {
                    imageUrls.add(image.getImgUrl());
                }
            }
            venueResponse.setImgUrl(imageUrls);
            venueResponse.setFavoriteSize(venue.getFavorites().size());
            List<Map<String, String>> commentsResponseList = new ArrayList<>();
            for (Comment comment : venue.getComments()) {
                Map<String, String> commentMap = new HashMap<>();
                commentMap.put("name", comment.getUser().getName());
                commentMap.put("surname", comment.getUser().getSurname());
                commentMap.put("comment", comment.getContent());
                commentMap.put("imgUrl", comment.getUser().getProfileImage().getImgUrl());

                commentsResponseList.add(commentMap);
            }
            Map<String, String> aboutMap = new HashMap<>();
            aboutMap.put("workingHour", venue.getWorkingHour());
            aboutMap.put("phoneNumber", venue.getPhoneNumber());
            aboutMap.put("address", venue.getAdress());
            aboutMap.put("webSite", venue.getWebsite());
            venueResponse.setAbout(aboutMap);
            return ResponseEntity.ok().body(venueResponse);
        }
         Map<String, String> responseMap = new HashMap<>();
         responseMap.put("message", "Kullanıcı başarılı bir şekilde kaydedildi.");
         return ResponseEntity.badRequest().body(responseMap);
     }

}