package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "PostPrivacyTypes")
public class PostPrivacyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "privacy_id")
    private Integer privacyId;

    @Column(name = "privacy_name", nullable = false, unique = true)
    private String privacyName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 