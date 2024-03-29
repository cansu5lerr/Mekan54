package com.example.mekan54.security.service;

import com.example.mekan54.model.*;
import com.example.mekan54.payload.request.*;
import com.example.mekan54.payload.response.JwtResponse;
import com.example.mekan54.payload.response.MessageResponse;
import com.example.mekan54.payload.response.UserDetailsResponse;
import com.example.mekan54.repository.CategoryRepository;
import com.example.mekan54.repository.RoleRepository;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import com.example.mekan54.security.jwt.EmailPasswordAuthenticationToken;
import com.example.mekan54.security.jwt.JwtUtils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Service

public class UserAuthService implements UserDetailsService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    VenueRepository venueRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new EmailPasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            return ResponseEntity
                    .ok().body(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(), roles));
        } catch (AuthenticationException e) {
            // Kullanıcı girişi başarısız oldu
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("E posta adresi veya parola hatalıdır.");
        }
    }

 public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
     try {
         String[] fieldsToCheck = {"Ad", "Soyad", "Email", "Sifre", "SifreTekrarı"};

         for (String field : fieldsToCheck) {
             String value = getValue(registerRequest, field);
             if (value == null || value.isEmpty()) {
                 return ResponseEntity.badRequest().body(field + " boş geçilemez.");
             }
         }
         String email = registerRequest.getEmail();
         String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
         Pattern pattern = Pattern.compile(emailRegex);
         if (!pattern.matcher(email).matches()) {
             return ResponseEntity.badRequest().body("Email formatı uygun değil.");

         }
         if (userRepository.existsByEmail(registerRequest.getEmail())) {
             return ResponseEntity.badRequest().body("Bu email adresi alınmıştır.");
         }
         if (!registerRequest.getPassword().equals(registerRequest.getRepeatPassword())) {
             return ResponseEntity.badRequest().body("Şifre tekrarında hata var.");
         }
         // Create new user's account
         User user = new User(registerRequest.getName(), registerRequest.getSurname(), registerRequest.getEmail(),
                 encoder.encode(registerRequest.getPassword()));
         Set<Role> roles = new HashSet<>();
         Optional<Role> userRole = roleRepository.findByName(ERole.ROLE_USER);

         Role role;
         if (!userRole.isPresent()) {
             Role newRole = new Role();
             newRole.setName(ERole.ROLE_USER);
             roleRepository.save(newRole);
             role = newRole;
         } else {
             role = userRole.get();
         }

         roles.add(role);
         user.setRoles(roles);
         userRepository.save(user);
         Map<String, String> responseMap = new HashMap<>();
         responseMap.put("message", "Kullanıcı başarılı bir şekilde kaydedildi.");
         return ResponseEntity.ok().body(responseMap);
     } catch (Exception e) {

         e.printStackTrace();

         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body("Bir hata oluştu. Lütfen daha sonra tekrar deneyin.");
     }
 }

    private String getValue(RegisterRequest signupRequest, String field) {
        switch (field) {
            case "Ad":
                return signupRequest.getName();
            case "Soyad":
                return signupRequest.getSurname();
            case "Email":
                return signupRequest.getEmail();
            case "Sifre":
                return signupRequest.getPassword();
            case "SifreTekrarı":
                return signupRequest.getRepeatPassword();
            default:
                throw new IllegalArgumentException("Geçersiz alan adı: " + field);
        }
    }
    public ResponseEntity<?> resetPassword (String generatedPassword , ResetPassword resetPassword) {
        try {
            Optional<User> userOptional = userRepository.findByGenerateCode(generatedPassword);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (resetPassword.getPassword().equals(resetPassword.getRepeatPassword())) {
                    user.setPassword(encoder.encode(resetPassword.getPassword()));
                    user.setGenerateCode(null);
                    userRepository.save(user);
                    Map<String, String> messageResponse = new HashMap<>();
                    messageResponse.put("message", "Şifre sıfırlama işlemi başarılı.");
                    return ResponseEntity.ok().body(messageResponse);
                } else {
                    Map<String, String> messageResponse = new HashMap<>();
                    messageResponse.put("message", "Şifreler eşleşmiyor.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(messageResponse);
                }
            } else {
                Map<String, String> messageResponse = new HashMap<>();
                messageResponse.put("message", "Kod hatalı");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(messageResponse);            }
        } catch (Exception e) {
            Map<String, String> messageResponse = new HashMap<>();
            messageResponse.put("message", "Bir hata oluştu.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(messageResponse);
        }
    }

    public ResponseEntity <?>isGeneratedPassword(String generatedPassword) {
        Map<String, Object> messageResponse = new HashMap<>();
        if (generatedPassword == null || generatedPassword.trim().isEmpty()) {
            messageResponse.put("message", "Hatalı kod");
            messageResponse.put("isGenerated", false);
        } else {
            Optional<User> userOptional = userRepository.findByGenerateCode(generatedPassword);
            if (userOptional.isPresent() && userOptional.get().getGenerateCode() != null) {
                messageResponse.put("message", "Başarılı");
                messageResponse.put("isGenerated", true);
            } else {
                messageResponse.put("message", "Hatalı kod");
                messageResponse.put("isGenerated", false);
            }
        }
        return ResponseEntity.ok().body(messageResponse);
    }
    public ResponseEntity<?> updateUser (String token, UserUpdateRequest userUpdateRequest) {
        User user = userDetailsService.getAuthenticatedUserFromToken(token);
        if(user instanceof User) {
          if(!userUpdateRequest.getEmail().isEmpty()) {
              user.setEmail(userUpdateRequest.getEmail());
          }
          if(!userUpdateRequest.getName().isEmpty()) {
              user.setName(userUpdateRequest.getName());
          }
          if(!userUpdateRequest.getSurname().isEmpty()) {
              user.setSurname(userUpdateRequest.getSurname());
          }
          userRepository.save(user);

        }
        Map<String, String> messageResponse = new HashMap<>();
        messageResponse.put("message", "Kullanıcı güncellendi");
        return ResponseEntity.ok().body(messageResponse);

    }







}
