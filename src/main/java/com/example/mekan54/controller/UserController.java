package com.example.mekan54.controller;
import com.example.mekan54.payload.request.*;
import com.example.mekan54.security.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.mekan54.security.service.UserDetailsServiceImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    UserAuthService userAuthService;
    @Autowired
    UserDetailsServiceImpl userDetailsService;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return userAuthService.authenticateUser(loginRequest);
    }
    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        return userAuthService.registerUser(registerRequest);
    }
    @GetMapping("/user/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader("Authorization") String token) {
        return userDetailsService.getUserProfile(token);
    }
    @PostMapping("/user/generatedToken")
    public ResponseEntity<?> checkGeneratedPassword(@RequestBody CodeRequest codeRequest) {
    return userAuthService.isGeneratedPassword(codeRequest.getGenerateToken());
   }
   @PostMapping("/user/resetPassword")
    public ResponseEntity<?> resetPassword (@RequestHeader("Authorization") String generatedToken,@RequestBody ResetPassword resetPassword) {
       return userAuthService.resetPassword(generatedToken,resetPassword);
   }

   @PostMapping("/user/updateProfile")
    public ResponseEntity<?> updateProfile (@RequestHeader("Authorization") String token,@RequestBody UserUpdateRequest userUpdateRequest ) {
        return userAuthService.updateUser(token, userUpdateRequest);
   }




}