package org.example.repository;

import org.example.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiver_UseridOrderByCreatedAtDesc(Long receiverId);
    Long countByReceiver_UseridAndIsReadFalse(Long receiverId);
} 