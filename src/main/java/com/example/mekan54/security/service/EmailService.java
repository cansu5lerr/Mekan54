package com.example.mekan54.security.service;
import com.example.mekan54.model.User;
import com.example.mekan54.payload.request.EmailRequest;
import com.example.mekan54.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

   /* public void sendEmail(String token) {
        try {
            User authenticadedUser = userDetailsService.getAuthenticatedUserFromToken(token);
          if(authenticadedUser instanceof User) {
       SimpleMailMessage message = new SimpleMailMessage();
       String to = authenticadedUser.getEmail();
       message.setTo(to);
       String subject ="Parola Sıfırlama";
       message.setSubject(subject);
       String text = generateFiveDigitRandomCode();
       authenticadedUser.setGenerateCode(text);
       message.setText("Tek kullanımlık kodunuz: "+text);
       mailSender.send(message);
       userRepository.save(authenticadedUser);
        }
        } catch (MailException e) {
            e.printStackTrace();
        }
    } */
    public ResponseEntity<?> sendMail(EmailRequest emailRequest){
        try {
            Optional<User> userOptional = userRepository.findByEmail(emailRequest.getGenerateToken());
            if(userOptional.isPresent()){
                User user = userOptional.get();
                SimpleMailMessage message = new SimpleMailMessage();
                String to = emailRequest.getGenerateToken();
                message.setTo(to);
                String subject ="Parola Sıfırlama";
                message.setSubject(subject);
                String text = generateFiveDigitRandomCode();
                user.setGenerateCode(text);
                message.setText("Tek kullanımlık kodunuz: "+text);
                mailSender.send(message);
                userRepository.save(user);
            }
        }
        catch (MailException e) {
            e.printStackTrace();
        }
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(emailRequest.getGenerateToken(), " hesabınıza kod gönderildi. Kontrol ediniz.");
        return ResponseEntity.ok().body(responseMap);
    }
    public static String generateFiveDigitRandomCode() {
        StringBuilder code = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < 5; i++) {
            int digit = secureRandom.nextInt(10);
            code.append(digit);
        }
        return code.toString();
    }
}
