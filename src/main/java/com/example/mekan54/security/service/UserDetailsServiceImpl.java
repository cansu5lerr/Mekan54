package com.example.mekan54.security.service;

import com.example.mekan54.model.User;
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
import java.util.Optional;


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
        if (authenticatedUser instanceof User) {

            return ResponseEntity.ok(new UserDetailsResponse(authenticatedUser.getName(),
                    authenticatedUser.getSurname(),
                    authenticatedUser.getEmail()));
        }
        return ResponseEntity.badRequest().body("Kullanıcı bulunamadı");
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