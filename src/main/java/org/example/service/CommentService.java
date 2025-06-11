package org.example.service;

import org.example.entity.Comment;
import org.example.entity.Post;
import org.example.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> findByPostOrderByCreatedAtDesc(Post post) {
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }

    public long countByPost(Post post) {
        return commentRepository.countByPost(post);
    }

    public Optional<Comment> findById(Integer id) {
        return commentRepository.findById(id);
    }

    @Transactional
    public void delete(Comment comment) {
        commentRepository.delete(comment);
    }
} 