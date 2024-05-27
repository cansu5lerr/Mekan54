package com.example.mekan54.controller;

import com.example.mekan54.payload.request.ReservationIdRequest;
import com.example.mekan54.payload.request.ReservationRequest;
import com.example.mekan54.security.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class ReservationController {

    @Autowired
    ReservationService reservationService;
    @PostMapping("/addReservationUser")
    public ResponseEntity<?> addReservation (@RequestHeader("Authorization") String token, @RequestBody ReservationRequest reservationRequest) throws Exception {
        return reservationService.addReservationtheUser(token,reservationRequest);
    }
    @PostMapping("/addReservationVenue")
    public ResponseEntity<?> addReservationVenue (@RequestHeader("Authorization") String token,
                                                  @RequestBody ReservationIdRequest reservationIdRequest)
    {
        return reservationService.approvedReservation(token,reservationIdRequest);
    }
    @GetMapping("/listVenueReservation")
    public ResponseEntity <?> listVenueReservation (@RequestHeader("Authorization") String token) {
        return reservationService.listReservationVenue(token);
    }
    @GetMapping("/listUserReservation")
    public ResponseEntity<?> listUserReservation(@RequestHeader("Authorization") String token) {
        return reservationService.listReservationUser(token);
    }
    @DeleteMapping("/deleteUserReservation")
    public ResponseEntity<?> deleteUserReservation (@RequestHeader("Authorization") String token,
                                                    @RequestBody ReservationIdRequest reservationIdRequest) {
        return reservationService.deleteReservationUser(token,reservationIdRequest);
    }

    @DeleteMapping("/deleteVenueReservation")
    public ResponseEntity<?> deleteVenueReservation(@RequestHeader("Authorization") String token,
                                                    @RequestBody ReservationIdRequest reservationIdRequest)
    {
        return reservationService.deleteReservationVenue(token,reservationIdRequest);
    }

    @GetMapping("/listNotification")
    public ResponseEntity<?> listNotification(@RequestHeader("Authorization") String token) {
        return reservationService.listNotification(token);
    }


}
