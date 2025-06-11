package org.example.repository;

import org.example.entity.Comment;
import org.example.entity.CommentReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentReplyRepository extends JpaRepository<CommentReply, Integer> {
    List<CommentReply> findByCommentOrderByCreatedAtDesc(Comment comment);
    long countByComment(Comment comment);
} 