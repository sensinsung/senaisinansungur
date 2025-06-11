package org.example.repository;

import org.example.entity.CommentReply;
import org.example.entity.ReplyLike;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Integer> {
    boolean existsByReplyAndUser(CommentReply reply, User user);
    void deleteByReplyAndUser(CommentReply reply, User user);
    long countByReply(CommentReply reply);
    void deleteByReply(CommentReply reply);
}