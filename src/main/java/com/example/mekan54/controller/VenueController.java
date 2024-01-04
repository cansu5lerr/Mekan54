package com.example.mekan54.controller;

import com.example.mekan54.payload.request.CategoryNameRequest;
import com.example.mekan54.payload.request.RegisterAdminRequest;
import com.example.mekan54.payload.request.VenueNameRequest;
import com.example.mekan54.payload.request.VenueUpdateRequest;
import com.example.mekan54.security.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class VenueController {
    @Autowired
    VenueService venueService;
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
    public ResponseEntity<?> getVenueName(@RequestHeader("Authorization") String token ,@RequestBody VenueNameRequest venueNameResquest){
        return venueService.getVenuesByName(token,venueNameResquest.getVenueName());
   }
   @PostMapping("/updateVenue")
    public ResponseEntity<?> updateVenue(@RequestHeader("Authorization") String token , @RequestBody VenueUpdateRequest venueRequest) {
        return venueService.updateVenue(token,venueRequest);
   }
   @GetMapping("/venueOwner")
    public ResponseEntity<?> getVenueOwner(@RequestHeader("Authorization") String token) {
        return venueService.getVenueOwner(token);
   }

}



