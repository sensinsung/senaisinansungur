package org.example.controller;

import org.example.entity.Notification;
import org.example.service.AuthenticationService;
import org.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        Long userId = authenticationService.getCurrentUser().getUserid();
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        Long userId = authenticationService.getCurrentUser().getUserid();
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<?> markAllAsRead() {
        try {
            Long userId = authenticationService.getCurrentUser().getUserid();
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Bildirimler okundu olarak işaretlenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllNotifications() {
        try {
            Long userId = authenticationService.getCurrentUser().getUserid();
            notificationService.deleteAllNotifications(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Bildirimler silinirken bir hata oluştu: " + e.getMessage());
        }
    }
}