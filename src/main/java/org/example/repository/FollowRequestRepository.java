package org.example.repository;

import org.example.entity.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    boolean existsBySender_UseridAndReceiver_Userid(Long senderId, Long receiverId);
    List<FollowRequest> findByReceiver_Userid(Long receiverId);
} 