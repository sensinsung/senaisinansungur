package org.example.repository;

import org.example.entity.Post;
import org.example.entity.PostLike;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {
    PostLike findByPostAndUser(Post post, User user);
    long countByPost(Post post);
} 