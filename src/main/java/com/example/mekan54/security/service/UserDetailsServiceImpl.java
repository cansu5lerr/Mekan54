package com.example.mekan54.security.service;

import com.example.mekan54.model.Favorite;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Image;

import com.example.mekan54.model.Venue;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));
        return UserDetailsImpl.build(user);
    }

    public ResponseEntity<?> getUserProfile (String token) {
        User authenticatedUser = getAuthenticatedUserFromToken(token);
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        if (authenticatedUser instanceof User) {
            List<Favorite> FavoriteList = authenticatedUser.getFavorites();
            List<Map<String, Object>> venuesResponseList = new ArrayList<>();
            List<List<String>> imageUrlList = new ArrayList<>();
            for (Favorite favorite : FavoriteList) {
                Venue venue = favorite.getVenue();
                Map<String, Object> venueList = new HashMap<>();
                venueList.put("name", Objects.toString(venue.getVenueName(), "null"));
                venueList.put("category", Objects.toString(venue.getCategory().getCategoryName(), "null"));
                venueList.put("address", Objects.toString(venue.getAdress(), "null"));
                venueList.put("website", Objects.toString(venue.getWebsite(), "null"));
                venueList.put("workingHour", Objects.toString(venue.getWorkingHour(), "null"));
                List<Image> imageList = venue.getImages();
                List<String> imgList = new ArrayList<>();
               for(Image image : imageList) {
                   imgList.add(image.getImgUrl());
               }
                venueList.put("imageUrl", imgList);
                imageUrlList.add(imgList);
                venuesResponseList.add(venueList);
            }
            userDetailsResponse.setVenuesResponseList(venuesResponseList);
            userDetailsResponse.setEmail(authenticatedUser.getEmail());
            userDetailsResponse.setName(authenticatedUser.getName());
            userDetailsResponse.setSurname(authenticatedUser.getSurname());
            return ResponseEntity.ok().body(userDetailsResponse);
        }
        Map<String , String> responseMap = new HashMap<>();
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
