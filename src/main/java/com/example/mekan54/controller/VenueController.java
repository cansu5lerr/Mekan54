package com.example.mekan54.controller;

import com.example.mekan54.payload.request.CategoryNameRequest;
import com.example.mekan54.payload.request.RegisterAdminRequest;
import com.example.mekan54.payload.request.VenueNameRequest;
import com.example.mekan54.payload.request.VenueUpdateRequest;
import com.example.mekan54.security.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class VenueController {
    @Autowired
    VenueService venueService;
    private static final Logger LOGGER = Logger.getLogger(VenueController.class.getName());

    @PostMapping("/owner/register")
    public ResponseEntity<?> addAdmin(@RequestBody RegisterAdminRequest registerAdminRequest) {
        return venueService.registerAdmin(registerAdminRequest);
    }
   @GetMapping("/venues")
    public ResponseEntity<?> getAllVenues(@RequestHeader("Authorization") String token ) {
        return venueService.getVenues(token);
   }
   @PostMapping("/categoryName")
   public ResponseEntity<?> getCategoryVenue(@RequestHeader("Authorization") String token ,@RequestBody CategoryNameRequest categoryNameRequest) {
        return venueService.getVenuesWithCategories(token,categoryNameRequest);
   }
   @PostMapping("/venueName")
    public ResponseEntity<?> getVenueName(@RequestHeader("Authorization") String token ,@RequestBody VenueNameRequest venueNameRequest){
       LOGGER.log(Level.INFO, "Aranan Venue : {0}", venueNameRequest);
       return venueService.getVenuesByName(token, String.valueOf((venueNameRequest)));
    }
  /* @PostMapping("/updateVenue")
    public ResponseEntity<?> updateVenue(@RequestHeader("Authorization") String token , @RequestBody VenueUpdateRequest venueRequest) {
       LOGGER.log(Level.INFO, "updateVenue API called with token: {0}", token);
       LOGGER.log(Level.INFO, "VenueUpdateRequest: {0}", venueRequest);

       ResponseEntity<?> responseEntity = venueService.updateVenue(token, venueRequest);

       LOGGER.log(Level.INFO, "updateVenue API response: {0}", responseEntity.getBody());
       return responseEntity;
   }  */
  /*@PostMapping("/updateVenue")
  public ResponseEntity<?> updateVenue(@RequestHeader("Authorization") String token, @RequestBody VenueUpdateRequest venueRequest) {
      if(token != null) {
          Map<String, String> responseMapp = new HashMap<>();
          responseMapp.put("message", token);
          return  ResponseEntity.ok().body(responseMapp);
      }
      if (venueRequest == null || isAnyFieldEmpty(venueRequest)) {
          Map<String, String> errorMap = new HashMap<>();
          errorMap.put("message", "Venue güncelleme isteği boş olamaz.");
          return ResponseEntity.badRequest().body(errorMap);
      }

      ResponseEntity<?> responseEntity = venueService.updateVenue(token, venueRequest);

      if (responseEntity == null) {
          Map<String, String> errorMap = new HashMap<>();
          errorMap.put("message", "Güncelleme işlemi sırasında bir hata oluştu.");
          return ResponseEntity.badRequest().body(errorMap);
      } else {
          return responseEntity;
      }
  } */
  @PostMapping("/updateVenue")
  public ResponseEntity<?> updateVenue(@RequestHeader("Authorization") String token, @RequestBody VenueUpdateRequest venueRequest) {
   return venueService.updateVenue(token,venueRequest);
  }
   @GetMapping("/venueOwner")
    public ResponseEntity<?> getVenueOwner(@RequestHeader("Authorization") String token) {
        return venueService.getVenueOwner(token);
   }

    private boolean isAnyFieldEmpty(VenueUpdateRequest venueRequest) {
        return venueRequest.getVenueName().isEmpty() ||
                venueRequest.getAdress().isEmpty() ||
                venueRequest.getPhoneNumber().isEmpty() ||
                venueRequest.getWebsite().isEmpty() ||
                venueRequest.getWorkingHour().isEmpty() ||
                venueRequest.getCategoryName().isEmpty();
    }
}



