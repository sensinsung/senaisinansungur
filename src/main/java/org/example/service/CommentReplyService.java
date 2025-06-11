package org.example.service;

import org.example.entity.Comment;
import org.example.entity.CommentReply;
import org.example.repository.CommentReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class CommentReplyService {
    @Autowired
    private CommentReplyRepository commentReplyRepository;

    public CommentReply save(CommentReply reply) {
        return commentReplyRepository.save(reply);
    }

    public List<CommentReply> findByCommentOrderByCreatedAtDesc(Comment comment) {
        return commentReplyRepository.findByCommentOrderByCreatedAtDesc(comment);
    }

    public long countByComment(Comment comment) {
        return commentReplyRepository.countByComment(comment);
    }

    public Optional<CommentReply> findById(Integer id) {
        return commentReplyRepository.findById(id);
    }

    @Transactional
    public void delete(CommentReply reply) {
        commentReplyRepository.delete(reply);
    }
} 