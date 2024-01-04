package com.example.mekan54.controller;

import com.example.mekan54.payload.request.EmailRequest;
import com.example.mekan54.security.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /*@GetMapping("/sendEmail")
    public ResponseEntity<String> sendEmail(@RequestHeader("Authorization") String token) {
        emailService.sendEmail(token);
        return ResponseEntity.ok("Mail adresinizi kontrol ediniz.");
    } */
    @PostMapping("/sendEmail")
    public ResponseEntity <?> sendEmail (@RequestBody EmailRequest emailRequest) {
       return emailService.sendMail(emailRequest);
    }

}
