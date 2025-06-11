package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_photos")
public class PostPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Integer photoId;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "photo_data", nullable = false)
    @Lob
    private byte[] photoData;

    @Column(name = "photo_order", nullable = false)
    private Integer photoOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 