package org.example.service;

import org.example.entity.CommentReply;
import org.example.entity.ReplyLike;
import org.example.entity.User;
import org.example.repository.ReplyLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReplyLikeService {
    @Autowired
    private ReplyLikeRepository replyLikeRepository;

    public boolean toggleLike(CommentReply reply, User user) {
        if (existsByReplyAndUser(reply, user)) {
            replyLikeRepository.deleteByReplyAndUser(reply, user);
            return false;
        } else {
            replyLikeRepository.save(new ReplyLike(reply, user));
            return true;
        }
    }

    public boolean existsByReplyAndUser(CommentReply reply, User user) {
        return replyLikeRepository.existsByReplyAndUser(reply, user);
    }

    public long countByReply(CommentReply reply) {
        return replyLikeRepository.countByReply(reply);
    }

    @Transactional
    public void deleteByReply(CommentReply reply) {
        replyLikeRepository.deleteByReply(reply);
    }
} 