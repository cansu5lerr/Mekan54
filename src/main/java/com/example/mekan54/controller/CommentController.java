package com.example.mekan54.controller;

import com.example.mekan54.payload.request.CommentRequest;
import com.example.mekan54.security.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class CommentController {
    @Autowired
    CommentService commentService;
    @PostMapping("/addComment/{venueId}")
    public ResponseEntity<?> addComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long venueId,
            @RequestBody CommentRequest commentRequest)
    {
        return commentService.addComment(token,venueId,commentRequest);
    }

    @DeleteMapping("/deleteComment/{venueId}/{commentId}")
    public ResponseEntity<?> deleteComment (
            @RequestHeader("Authorization") String token,
            @PathVariable Long venueId,
            @PathVariable Long commentId
    )
    {
        return commentService.deleteComment(token,venueId,commentId);
    }

}
