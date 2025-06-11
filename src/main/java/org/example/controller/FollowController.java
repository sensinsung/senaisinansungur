package org.example.controller;

import org.example.entity.User;
import org.example.entity.FollowRequest;
import org.example.service.AuthenticationService;
import org.example.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        followService.followUser(currentUser.getUserid(), userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId) {
        User currentUser = authenticationService.getCurrentUser();
        followService.unfollowUser(currentUser.getUserid(), userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/followers")
    public ResponseEntity<List<User>> getFollowers() {
        User currentUser = authenticationService.getCurrentUser();
        return ResponseEntity.ok(followService.getFollowers(currentUser.getUserid()));
    }

    @GetMapping("/following")
    public ResponseEntity<List<User>> getFollowing() {
        User currentUser = authenticationService.getCurrentUser();
        return ResponseEntity.ok(followService.getFollowing(currentUser.getUserid()));
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<?> getUserFollowers(@PathVariable Long userId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            List<User> followers = followService.getFollowers(userId);
            
            List<Map<String, Serializable>> followersWithStatus = followers.stream()
                .map(follower -> {
                    Map<String, Serializable> userMap = new HashMap<>();
                    userMap.put("userid", follower.getUserid());
                    userMap.put("firstName", follower.getFirstName());
                    userMap.put("lastName", follower.getLastName());
                    userMap.put("username", follower.getUsername());
                    userMap.put("profilePicture", follower.getProfilePicture());
                    userMap.put("isFollowing", currentUser != null && followService.isFollowing(currentUser.getUserid(), follower.getUserid()));
                    return userMap;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(followersWithStatus);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Takipçiler getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<?> getUserFollowing(@PathVariable Long userId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            List<User> following = followService.getFollowing(userId);
            
            List<Map<String, Serializable>> followingWithStatus = following.stream()
                .map(followed -> {
                    Map<String, Serializable> userMap = new HashMap<>();
                    userMap.put("userid", followed.getUserid());
                    userMap.put("firstName", followed.getFirstName());
                    userMap.put("lastName", followed.getLastName());
                    userMap.put("username", followed.getUsername());
                    userMap.put("profilePicture", followed.getProfilePicture());
                    userMap.put("isFollowing", currentUser != null && followService.isFollowing(currentUser.getUserid(), followed.getUserid()));
                    return userMap;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(followingWithStatus);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Takip edilenler getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/remove-follower/{followerId}")
    public ResponseEntity<?> removeFollower(@PathVariable Long followerId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            followService.unfollowUser(followerId, currentUser.getUserid());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Takipçi kaldırılırken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<?> sendFollowRequest(@PathVariable Long userId) {
        try {
            User currentUser = authenticationService.getCurrentUser();
            followService.sendFollowRequest(currentUser.getUserid(), userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Takip isteği gönderilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications() {
        try {
            User currentUser = authenticationService.getCurrentUser();
            List<FollowRequest> notifications = followService.getNotifications(currentUser.getUserid());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Bildirimler getirilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/request/{requestId}/accept")
    public ResponseEntity<?> acceptFollowRequest(@PathVariable Long requestId) {
        try {
            followService.acceptFollowRequest(requestId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Takip isteği kabul edilirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PostMapping("/request/{requestId}/reject")
    public ResponseEntity<?> rejectFollowRequest(@PathVariable Long requestId) {
        try {
            followService.rejectFollowRequest(requestId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Takip isteği reddedilirken bir hata oluştu: " + e.getMessage());
        }
    }
} 