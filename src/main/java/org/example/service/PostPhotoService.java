package org.example.service;

import org.example.entity.Post;
import org.example.entity.PostPhoto;
import org.example.repository.PostPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PostPhotoService {
    
    @Autowired
    private PostPhotoRepository postPhotoRepository;
    
    public PostPhoto save(PostPhoto photo) {
        return postPhotoRepository.save(photo);
    }
    
    public List<PostPhoto> findByPostOrderByPhotoOrderAsc(Post post) {
        return postPhotoRepository.findByPostOrderByPhotoOrderAsc(post);
    }

    public Optional<PostPhoto> findById(Integer photoId) {
        return postPhotoRepository.findById(photoId);
    }

    public void delete(PostPhoto postPhoto) {
        postPhotoRepository.delete(postPhoto);
    }
} 