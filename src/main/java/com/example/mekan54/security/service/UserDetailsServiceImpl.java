package com.example.mekan54.security.service;

import com.example.mekan54.model.*;

import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.security.jwt.EmailPasswordAuthenticationToken;
import com.example.mekan54.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.mekan54.payload.response.UserDetailsResponse;
import com.example.mekan54.payload.response.VenueResponse;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;
    private static final Logger LOGGER = Logger.getLogger(VenueService.class.getName());

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));
        return UserDetailsImpl.build(user);
    }

    public ResponseEntity<?> getUserProfile(String token) {
        User authenticatedUser = getAuthenticatedUserFromToken(token);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();

        if (authenticatedUser instanceof User) {
            List<Favorite> favoriteList = authenticatedUser.getFavorites();
            List<VenueResponse> favoriteVenueList = new ArrayList<>();
            for (Favorite favorite : favoriteList) {
                Venue venue = favorite.getVenue();
                VenueResponse venueResponse = new VenueResponse();
                venueResponse.setVenueName(Objects.toString(venue.getVenueName(), "null"));
                venueResponse.setCategoryName(Objects.toString(venue.getCategory().getCategoryName(), "null"));
                LOGGER.log(Level.INFO, " venueFavorites: " + venue.getFavorites().size());
                venueResponse.setFavoriteSize(venue.getFavorites().size());
                venueResponse.setId(venue.getId());
                List<Image> imageList = venue.getImages();
                List<String> imgList = new ArrayList<>();
                for (Image image : imageList) {
                    imgList.add(image.getImgUrl());
                }
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
                    commentMap.put("id", comment.getId().toString());
                    commentsResponseList.add(commentMap);
                }
                venueResponse.setComments(commentsResponseList);
                Map<String, String> aboutMap = new HashMap<>();
                aboutMap.put("workingHour", Objects.toString(venue.getWorkingHour(), "null"));
                aboutMap.put("phoneNumber", Objects.toString(venue.getPhoneNumber(), "null"));
                aboutMap.put("address", Objects.toString(venue.getAdress(), "null"));
                aboutMap.put("webSite", Objects.toString(venue.getWebsite(), "null"));
                venueResponse.setAbout(aboutMap);
                venueResponse.setImgUrl(imgList);
                favoriteVenueList.add(venueResponse);
            }

            userDetailsResponse.setFavoriteVenueList(favoriteVenueList);
            userDetailsResponse.setEmail(authenticatedUser.getEmail());
            userDetailsResponse.setName(authenticatedUser.getName());
            userDetailsResponse.setSurname(authenticatedUser.getSurname());
            userDetailsResponse.setImgUrl(authenticatedUser.getProfileImage().getImgUrl());
            return ResponseEntity.ok().body(userDetailsResponse);
        }

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Kullanıcı bulunamadı.");
        return ResponseEntity.badRequest().body(responseMap);
    }


    //User güncellemesi
   /* public UserDetailsResponse updateUserProfile(String token , UserDetailsResponse userDetailsResponse) {
        String username = jwtUtils.extractEmail(token);
        UserDetails userDetails = null;
        if(username != null) {
            userDetails = loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Optional<User> userOptional = userRepository.findByEmail(username);
            if(userOptional.isPresent()){
                User user = userOptional.get();
                user.setId(user.getId());
                user.setPassword(user.getPassword());
                user.setName(userDetailsResponse.getName());
                user.setSurname(userDetailsResponse.getSurname());
                user.setEmail(userDetailsResponse.getEmail());
                user.setRoles(user.getRoles());
                userRepository.save(user);
            }
        }
        return userDetailsResponse;
    } */
    //Userın idsini çekiyoruz.
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
    //burada kullanıcı authentication işlemini yapıyorum

    public User getAuthenticatedUserFromToken(String token) {
        String email = jwtUtils.extractEmail(token);
        UserDetails userDetails = null;
        if (email != null) {
            userDetails = loadUserByUsername(email);
            Authentication authentication = new EmailPasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                return userOptional.get();
            }
        }
        // Eğer kullanıcı bulunamazsa veya token geçersizse null dönebilirsiniz veya bir hata işleyebilirsiniz.
       return null;
    }

}
