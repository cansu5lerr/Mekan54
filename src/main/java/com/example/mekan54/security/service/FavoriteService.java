package com.example.mekan54.security.service;
import com.example.mekan54.model.Favorite;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import com.example.mekan54.payload.request.FavoriteRequest;
import com.example.mekan54.repository.FavoriteRepository;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    FavoriteRepository favoriteRepository;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    public ResponseEntity <?> addFavorite (String token , Long venueId) {
        User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
        if(authenticatedUser instanceof User) {
            Optional <Venue> venueOptional = venueRepository.findById(venueId);
            if(venueOptional.isPresent()) {
                Venue venue = venueOptional.get();
                Favorite favorite = createFavorite(authenticatedUser,venue);
                List<Favorite> favoriteList = new ArrayList<>();
                favoriteList.add(favorite);
                venue.setFavorites(favoriteList);
                authenticatedUser.setFavorites(favoriteList);
                saveUserFavoriteAndVenue(authenticatedUser,favorite,venue);
                return ResponseEntity.ok().body("Favorilere eklendi!");
            }
            return ResponseEntity.badRequest().body("Favoriye eklenemedi.");
        }
        return ResponseEntity.badRequest().body("Kullanıcı bulunamadı.");
    }
    public ResponseEntity<?> deleteFavorite(String token, Long venueId) {
        User authenticatedUser =userDetailsService.getAuthenticatedUserFromToken(token);
        if(authenticatedUser instanceof User){
            Optional<Venue> venueOptional = venueRepository.findById(venueId);
            if(venueOptional.isPresent()) {
                Venue venue = venueOptional.get();
                Optional<Favorite> favoriteOptional = findFavoriteInVenue(authenticatedUser, venue);
               if(favoriteOptional.isPresent()) {
                   Favorite favorite = favoriteOptional.get();
                   venue.getFavorites().remove(favorite);
                   authenticatedUser.getFavorites().remove(favorite);
                   deleteFavorite(authenticatedUser, venue,favorite);
               return ResponseEntity.ok().body("Favorilerden kaldırıldı");
               }
              return ResponseEntity.badRequest().body("Favori kaldırılamadı.");
            }
         return ResponseEntity .badRequest().body("Mekan bulunamadı.");
        }
        return ResponseEntity.badRequest().body("Başarısız kullanıcı girişi.");
    }
    @Transactional
    private void deleteFavorite (User user, Venue venue, Favorite favorite) {
        favoriteRepository.delete(favorite);
        venueRepository.save(venue);
        userRepository.save(user);
    }
    private Favorite createFavorite (User user, Venue venue) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setVenue(venue);

        return favorite;
    }
    @Transactional
    private void saveUserFavoriteAndVenue(User authenticatedUser, Favorite favorite, Venue venue) {
        userRepository.save(authenticatedUser);
        favoriteRepository.save(favorite);
        venueRepository.save(venue);
    }
    private Optional<Favorite> findFavoriteInVenue(User user, Venue venue) {
        return venue.getFavorites().stream()
                .filter(favorite -> favorite.getUser().equals(user))
                .findFirst();
    }




}
