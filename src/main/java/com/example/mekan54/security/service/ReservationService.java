package com.example.mekan54.security.service;

import com.example.mekan54.model.*;
import com.example.mekan54.payload.request.ReservationIdRequest;
import com.example.mekan54.payload.request.ReservationRequest;
import com.example.mekan54.payload.response.NotificationResponse;
import com.example.mekan54.payload.response.UserReservationResponse;
import com.example.mekan54.payload.response.VenueReservationResponse;
import com.example.mekan54.repository.NotificationRepository;
import com.example.mekan54.repository.ReservationRepository;
import com.example.mekan54.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    NotificationRepository notificationRepository;
    public ResponseEntity<?> addReservationtheUser (String token, ReservationRequest reservationRequest){
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            String dateTimeString = reservationRequest.getDateTime();
            LocalDateTime dateTime = parseStringToLocalDateTime(dateTimeString);
            List<Reservation> userReservations = reservationRepository.findByUserAndDateTime(user, dateTime);
            if(!userReservations.isEmpty()) {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("error", "Bu randevu zaten alınmışsınız!");
                return ResponseEntity.badRequest().body(responseMap);
            }

            Optional<Venue> optionalVenue = venueRepository.findById(reservationRequest.getVenueId());
            Venue venue = optionalVenue.get();
            List<Reservation> venueReservations = reservationRepository.findByVenueAndDateTime(venue, dateTime);
            if(!venueReservations.isEmpty()) {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("error", "Bu randevu dolu alınmış!");
                return ResponseEntity.badRequest().body(responseMap);
            }
            Reservation reservation = new Reservation();
            reservation.setUser(user);
            reservation.setVenue(venue);
            reservation.setTotalPeople(reservationRequest.getTotalPeople());
            reservation.setDateAndTime(dateTime);
            reservation.setMessage(reservationRequest.getMessage());
            reservation.setStatus(ReservationStatus.PENDING);
            reservationRepository.save(reservation);
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Randevu talebiniz gönderildi!");
            return ResponseEntity.ok().body(responseMap);
        }
        return null;
    }

    public ResponseEntity<?> approvedReservation(String token, ReservationIdRequest reservationIdRequest) {
        User venueOwner = userDetailsService.getAuthenticatedUserFromToken(token);
        if(venueOwner instanceof User) {
            Optional<Reservation> optionalReservation = reservationRepository.findById(reservationIdRequest.getId());
            if(optionalReservation.isPresent()){
                Reservation reservation= optionalReservation.get();
                Venue venue= reservation.getVenue();
                if(venue.getUser().equals(venueOwner)){
                    reservation.setStatus(ReservationStatus.APPROVED);
                    reservationRepository.save(reservation);
                    Map<String, String> responseMap = new HashMap<> ();
                    responseMap.put("message","Rezervasyon onaylandı!");
                    return ResponseEntity.ok().body(responseMap);
                }
                else {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("error", "Bu rezervasyon size ait değil!");
                    return ResponseEntity.badRequest().body(responseMap);
                }
            }
        }
        Map<String, String> responseMap = new HashMap<> ();
        responseMap.put("error","Kullanıcı girişi hatalı.");
        return ResponseEntity.badRequest().body(responseMap);
    }
    public ResponseEntity<?> listReservationUser (String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            Set<Reservation> reservations = user.getReservations();
            LocalDateTime currentDateTime = LocalDateTime.now();
            List<UserReservationResponse> userReservationResponseList = reservations.stream()
                    .filter(reservation -> reservation.getDateTime().isAfter(currentDateTime))
                    .sorted(Comparator.comparing(Reservation::getDateTime))
                    .map(reservation -> {
                        UserReservationResponse userReservationResponse = new UserReservationResponse();
                        userReservationResponse.setStatus(reservation.getStatus());
                        userReservationResponse.setTotalPeople(reservation.getTotalPeople());
                        userReservationResponse.setDateTime(parseLocalDateTimeToString(reservation.getDateTime()));
                        userReservationResponse.setVenueName(reservation.getVenue().getVenueName());
                        userReservationResponse.setMessage(reservation.getMessage());
                        userReservationResponse.setReservationId(reservation.getId());
                        return userReservationResponse;
                    })
                    .collect(Collectors.toList());

            if (!userReservationResponseList.isEmpty()) {
                return ResponseEntity.ok().body(userReservationResponseList);
            } else {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "Geçerli rezervasyon bulunmamaktadır.");
                return ResponseEntity.ok().body(responseMap);
            }
        }
        Map<String, String> responseMap = new HashMap<> ();
        responseMap.put("error","Kullanıcı girişi hatalı.");
        return ResponseEntity.badRequest().body(responseMap);
    }
    public ResponseEntity<?> listReservationVenue(String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if (user != null) {
            Optional<Venue> venueOptional = venueRepository.findById(user.getVenue().getId());
            if (venueOptional.isPresent()) {
                Venue venue = venueOptional.get();
                Set<Reservation> reservations = venue.getReservations();
                if (!reservations.isEmpty()) {
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    try {
                        List<VenueReservationResponse> venueReservationResponseList = reservations.stream()
                                .filter(reservation -> {
                                    LocalDateTime dateTime = reservation.getDateTime();
                                    if (dateTime == null) {
                                        throw new IllegalStateException("Reservation dateTime is null for reservation ID: " + reservation.getId());
                                    }
                                    return dateTime.isAfter(currentDateTime) && reservation.getStatus() != ReservationStatus.CANCELLED;
                                })
                                .sorted(Comparator.comparing(Reservation::getDateTime))
                                .map(reservation -> {
                                    VenueReservationResponse venueReservationResponse = new VenueReservationResponse();
                                    venueReservationResponse.setStatus(reservation.getStatus());
                                    venueReservationResponse.setTotalPeople(reservation.getTotalPeople());
                                    venueReservationResponse.setTotalPeople(reservation.getTotalPeople());
                                    venueReservationResponse.setDateTime(parseLocalDateTimeToString(reservation.getDateTime()));
                                    venueReservationResponse.setUserName(reservation.getUser().getName());
                                    venueReservationResponse.setUserSurname(reservation.getUser().getSurname());
                                    venueReservationResponse.setReservationId(reservation.getId());
                                    venueReservationResponse.setMessage(reservation.getMessage());
                                    return venueReservationResponse;
                                })
                                .collect(Collectors.toList());

                        return ResponseEntity.ok().body(venueReservationResponseList);

                    } catch (RuntimeException e) {
                        Map<String, String> responseMap = new HashMap<>();
                        responseMap.put("error", "Rezervasyonları işlerken bir hata oluştu: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
                    }
                } else {
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("message", "Rezervasyon bulunmamaktadır.");
                    return ResponseEntity.ok().body(responseMap);
                }
            }
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı.");
        return ResponseEntity.badRequest().body(responseMap);
    }


    public ResponseEntity<?> deleteReservationVenue(String token, Long reservationId) {
        User venueOwner = userDetailsService.getAuthenticatedUserFromToken(token);
        if(venueOwner instanceof User) {
            Optional <Reservation> optionalReservation = reservationRepository.findById(reservationId);
            if(optionalReservation.isPresent()) {
                Reservation reservation= optionalReservation.get();
                Venue venue = reservation.getVenue();
                if(venue.getUser().equals(venueOwner)) {
                    reservation.setStatus((ReservationStatus.CANCELLED));
                    reservationRepository.save(reservation);
                    sendCancellationMessage(reservation.getUser(),reservation.getDateTime(), venue);
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("message", "Rezervasyon iptal edildi ve kullanıcıya bildirim gönderildi!");
                    return ResponseEntity.ok().body(responseMap);
                }
            }
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı veya belirtilen rezervasyon bulunamadı.");
        return ResponseEntity.badRequest().body(responseMap);
    }
  /*  public ResponseEntity<?> deleteReservationVenue (String token, ReservationIdRequest reservationIdRequest) {
        User venueOwner = userDetailsService.getAuthenticatedUserFromToken(token);
        if(venueOwner instanceof User) {
            Optional <Reservation> optionalReservation = reservationRepository.findById(reservationIdRequest.getId());
            if(optionalReservation.isPresent()) {
                Reservation reservation= optionalReservation.get();
                Venue venue = reservation.getVenue();
                if(venue.getUser().equals(venueOwner)) {
                    reservation.setStatus((ReservationStatus.CANCELLED));
                    reservationRepository.save(reservation);
                    sendCancellationMessage(reservation.getUser(),reservation.getDateTime(), venue);
                    Map<String, String> responseMap = new HashMap<>();
                    responseMap.put("message", "Rezervasyon iptal edildi ve kullanıcıya bildirim gönderildi!");
                    return ResponseEntity.ok().body(responseMap);
                }
            }
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı veya belirtilen rezervasyon bulunamadı.");
        return ResponseEntity.badRequest().body(responseMap);
    } */


    public ResponseEntity<?> deleteReservationUser(String token, Long reservationId) {
        User user  = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            Reservation reservation = reservationRepository.getReferenceById(reservationId);
            User uservenue = reservation.getVenue().getUser();
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            sendAdminCancellationMessage(user,uservenue,reservation.getDateTime());
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Rezervasyon iptal edildi ve kullanıcıya bildirim gönderildi!");
            return ResponseEntity.ok().body(responseMap);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı veya belirtilen rezervasyon bulunamadı.");
        return ResponseEntity.badRequest().body(responseMap);
    }
    public ResponseEntity<?> deleteReservationUser(String token, ReservationIdRequest reservationIdRequest) {
        User user  = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            Reservation reservation = reservationRepository.getReferenceById(reservationIdRequest.getId());
            User uservenue = reservation.getVenue().getUser();
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            sendAdminCancellationMessage(user,uservenue,reservation.getDateTime());
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Rezervasyon iptal edildi ve kullanıcıya bildirim gönderildi!");
            return ResponseEntity.ok().body(responseMap);
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı veya belirtilen rezervasyon bulunamadı.");
        return ResponseEntity.badRequest().body(responseMap);
    }

    public ResponseEntity<?> listNotification (String token) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
            List<Notification> notificationList = user.getNotifications();
            if(!notificationList.isEmpty()){
                List <NotificationResponse> notificationResponseList = new ArrayList<>();
                for(Notification notification : notificationList) {
                    NotificationResponse notificationResponse = new NotificationResponse();
                    notificationResponse.setMessage(notification.getMessage());
                    notificationResponse.setDateTime(notification.getDateTime());
                    notificationResponseList.add(notificationResponse);
                }
                return ResponseEntity.ok().body(notificationResponseList);
            }
            else {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("message", "Bildirim yok!");
                return ResponseEntity.ok().body(responseMap);
            }
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("error", "Kullanıcı girişi hatalı veya belirtilen rezervasyon bulunamadı.");
        return ResponseEntity.badRequest().body(responseMap);
    }

    private void sendCancellationMessage(User user, LocalDateTime dateTime, Venue venue) {
        String notificationMessage = "Sayın " + user.getName() + ",\n\n"
                +venue.getVenueName() + " " +dateTime.toString()
                + " tarihinde yapmış olduğunuz rezervasyonunuz onaylanmamıştır.";
        Notification notification = new Notification();
        notification.setMessage(notificationMessage);
        notification.setDateTime(LocalDateTime.now());
        notification.setUser(user);
        notificationRepository.save(notification);
    }
    private void sendAdminCancellationMessage(User user, User venueuser, LocalDateTime dateTime) {
        String notificationMessage = "Sayın ilgili"
                + " " +dateTime.toString()
                + " tarihinde yapılmış olan "+ user.getName()+ " "+ user.getSurname()
                + "isimli kullanıcı randevuyu iptal etmiştir.";
        Notification notification = new Notification();
        notification.setMessage(notificationMessage);
        notification.setDateTime(LocalDateTime.now());
        notification.setUser(venueuser);
        notificationRepository.save(notification);
    }

    private LocalDateTime parseStringToLocalDateTime(String dateTimeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String parseLocalDateTimeToString(LocalDateTime dateTime) {
            DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return dateTime.format(FORMATTER);
    }



}

