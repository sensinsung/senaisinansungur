package org.example.repository;

import org.example.entity.User;
import org.example.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    @Query("SELECT f FROM Follow f WHERE f.follower.userid = :followerId AND f.following.userid = :followingId")
    Follow findByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.userid = :followerId AND f.following.userid = :followingId")
    void deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    
    @Query("SELECT f.follower FROM Follow f WHERE f.following.userid = :userId")
    List<User> findFollowersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT f.following FROM Follow f WHERE f.follower.userid = :userId")
    List<User> findFollowingByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.userid = :followerId AND f.following.userid = :followingId")
    boolean existsByFollowerIdAndFollowingId(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
} 