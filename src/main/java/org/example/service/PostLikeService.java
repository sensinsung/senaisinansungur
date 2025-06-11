package org.example.service;

import org.example.entity.Post;
import org.example.entity.PostLike;
import org.example.entity.User;
import org.example.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostLikeService {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Transactional
    public boolean toggleLike(Post post, User user) {
        PostLike existingLike = postLikeRepository.findByPostAndUser(post, user);
        
        if (existingLike != null) {
            postLikeRepository.delete(existingLike);
            return false;
        } else {
            PostLike newLike = new PostLike();
            newLike.setPost(post);
            newLike.setUser(user);
            newLike.setCreatedAt(java.time.LocalDateTime.now());
            postLikeRepository.save(newLike);
            return true;
        }
    }

    public boolean existsByPostAndUser(Post post, User user) {
        return postLikeRepository.findByPostAndUser(post, user) != null;
    }

    public long countByPost(Post post) {
        return postLikeRepository.countByPost(post);
    }
} 