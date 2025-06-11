package org.example.service;

import org.example.entity.User;
import org.example.entity.Follow;
import org.example.entity.FollowRequest;
import org.example.entity.Notification;
import org.example.repository.FollowRepository;
import org.example.repository.FollowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public FollowService(FollowRepository followRepository,
                        FollowRequestRepository followRequestRepository,
                        UserService userService,
                        NotificationService notificationService) {
        this.followRepository = followRepository;
        this.followRequestRepository = followRequestRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Kendinizi takip edemezsiniz.");
        }

        User follower = userService.findById(followerId);
        User following = userService.findById(followingId);

        if (follower == null || following == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı.");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalArgumentException("Bu kullanıcıyı zaten takip ediyorsunuz.");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId);
        if (follow != null) {
            followRepository.delete(follow);
        }
    }

    public List<User> getFollowers(Long userId) {
        return followRepository.findFollowersByUserId(userId);
    }

    public List<User> getFollowing(Long userId) {
        return followRepository.findFollowingByUserId(userId);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional
    public void sendFollowRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Kendinize takip isteği gönderemezsiniz.");
        }

        User sender = userService.findById(senderId);
        User receiver = userService.findById(receiverId);

        if (sender == null || receiver == null) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı.");
        }

        if (!receiver.getIsPrivate()) {
            throw new IllegalArgumentException("Bu kullanıcı gizli hesap değil.");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(senderId, receiverId)) {
            throw new IllegalArgumentException("Bu kullanıcıyı zaten takip ediyorsunuz.");
        }

        if (followRequestRepository.existsBySender_UseridAndReceiver_Userid(senderId, receiverId)) {
            throw new IllegalArgumentException("Bu kullanıcıya zaten takip isteği gönderdiniz.");
        }

        FollowRequest request = new FollowRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        followRequestRepository.save(request);
    }

    public List<FollowRequest> getNotifications(Long userId) {
        return followRequestRepository.findByReceiver_Userid(userId);
    }

    @Transactional
    public void acceptFollowRequest(Long requestId) {
        FollowRequest request = followRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Takip isteği bulunamadı."));

        Follow follow = new Follow();
        follow.setFollower(request.getSender());
        follow.setFollowing(request.getReceiver());
        followRepository.save(follow);

        Notification notification = new Notification();
        notification.setType(notificationService.getNotificationTypeByName("takip_onay"));
        notification.setSender(request.getReceiver());
        notification.setReceiver(request.getSender());
        notification.setContent(request.getReceiver().getUsername() + " takip isteğinizi kabul etti.");
        notification.setRequestId(requestId);
        notificationService.saveNotification(notification);

        followRequestRepository.delete(request);
    }

    @Transactional
    public void rejectFollowRequest(Long requestId) {
        FollowRequest request = followRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Takip isteği bulunamadı."));
        
        followRequestRepository.delete(request);
    }
} 