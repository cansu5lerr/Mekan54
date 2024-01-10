package com.example.mekan54.security.service;

import com.example.mekan54.model.Comment;
import com.example.mekan54.model.Favorite;
import com.example.mekan54.model.User;
import com.example.mekan54.model.Venue;
import com.example.mekan54.payload.request.CommentRequest;
import com.example.mekan54.payload.response.CommentResponse;
import com.example.mekan54.repository.CommentRepository;
import com.example.mekan54.repository.UserRepository;
import com.example.mekan54.repository.VenueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CommentService {

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserDetailsServiceImpl userDetailsService;
    @Autowired
    VenueRepository venueRepository;
    @Autowired
    UserRepository userRepository;
  public ResponseEntity<?> addComment(String token, Long venueId, CommentRequest commentRequest) {
      User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
      Map<String, String> messageResponse = new HashMap<>();
      if (authenticatedUser instanceof User) {
          Optional<Venue> venueOptional = venueRepository.findById(venueId);

          if(commentRequest.getComment().isEmpty()){

              messageResponse.put("error","Yorum yapılamadı.");
              return ResponseEntity.badRequest().body(messageResponse);
          }
          if (venueOptional.isPresent()) {
              Venue venue = venueOptional.get();
              Comment comment = createComment(authenticatedUser, venue, commentRequest);
              CommentResponse commentResponse = createCommentResponse(authenticatedUser, commentRequest);
              List<Comment> commentList = new ArrayList<>();
              commentList.add(comment);
              authenticatedUser.setComments(commentList);
              venue.setComments(commentList);
              commentRepository.save(comment);
              userRepository.save(authenticatedUser);
              venueRepository.save(venue);
              messageResponse.put("message","Yorum eklendi.");
              return ResponseEntity.badRequest().body(messageResponse);
          } else {
              messageResponse.put("error","Yorum yapılamadı.");
              return ResponseEntity.ok().body(messageResponse);
          }
      }
      messageResponse.put("error","Kullanıcı bulunamadı");
      return ResponseEntity.ok().body(messageResponse);
  }

  public ResponseEntity <?> deleteComment(String token, Long venueId, Long commentId) {
      User authenticatedUser = userDetailsService.getAuthenticatedUserFromToken(token);
      Map<String, String> messageResponse = new HashMap<>();
      if(authenticatedUser instanceof  User) {
          Optional <Venue> venueOptional = venueRepository.findById(venueId);
     if(venueOptional.isPresent()) {
         Venue venue= venueOptional.get();
         Optional <Comment> commentOptional= commentRepository.findById(commentId);
        if(commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            venue.getComments().remove(comment);
            authenticatedUser.getComments().remove(comment);
            deleteComment(authenticatedUser,venue,comment);
            messageResponse.put("error","Yorum kaldırıldı.");
            return ResponseEntity.ok().body(messageResponse);
        }
         messageResponse.put("error","Yorum kaldırılamadı.");
        return ResponseEntity.badRequest().body(messageResponse);
     }
          messageResponse.put("error","Mekan bulunamadı");
     return ResponseEntity.badRequest().body(messageResponse);
      }
      messageResponse.put("error","Kullanıcı bulunamadı.");
      return ResponseEntity.badRequest().body(messageResponse);
  }
    @Transactional
    private void deleteComment (User user, Venue venue, Comment comment) {
        commentRepository.delete(comment);
        venueRepository.save(venue);
        userRepository.save(user);
    }
    private Comment createComment(User user, Venue venue, CommentRequest commentRequest) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setVenue(venue);
        comment.setContent(commentRequest.getComment());
        return comment;
    }

    private CommentResponse createCommentResponse(User user, CommentRequest commentRequest) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setName(user.getName());
        commentResponse.setSurname(user.getSurname());
        commentResponse.setComment(commentRequest.getComment());
        return commentResponse;
    }




}
