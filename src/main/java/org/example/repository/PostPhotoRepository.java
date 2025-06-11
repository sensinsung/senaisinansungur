package org.example.repository;

import org.example.entity.Post;
import org.example.entity.PostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostPhotoRepository extends JpaRepository<PostPhoto, Integer> {
    List<PostPhoto> findByPostOrderByPhotoOrderAsc(Post post);
} 