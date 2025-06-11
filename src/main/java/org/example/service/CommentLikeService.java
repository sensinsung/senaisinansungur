package org.example.service;

import org.example.entity.Comment;
import org.example.entity.CommentLike;
import org.example.entity.User;
import org.example.repository.CommentLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentLikeService {
    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public boolean toggleLike(Comment comment, User user) {
        boolean liked = commentLikeRepository.existsByCommentAndUser(comment, user);
        if (liked) {
            commentLikeRepository.deleteByCommentAndUser(comment, user);
            return false;
        } else {
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(user);
            commentLikeRepository.save(like);
            return true;
        }
    }

    public long countByComment(Comment comment) {
        return commentLikeRepository.countByComment(comment);
    }

    public boolean existsByCommentAndUser(Comment comment, User user) {
        return commentLikeRepository.existsByCommentAndUser(comment, user);
    }
} 