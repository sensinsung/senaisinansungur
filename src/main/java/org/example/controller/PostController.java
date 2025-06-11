package org.example.controller;

import org.example.entity.*;
import org.example.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/post")
public class PostController {
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    
    private final PostService ps;
    private final UserService us;
    private final PostLikeService pls;
    private final CommentService cs;
    private final PostPhotoService pps;
    private final FollowService followService;

    public PostController(PostService ps,
                         UserService us,
                         PostLikeService pls,
                         CommentService cs,
                         PostPhotoService pps,
                         FollowService followService) {
        this.ps = ps;
        this.us = us;
        this.pls = pls;
        this.cs = cs;
        this.pps = pps;
        this.followService = followService;
    }

    @GetMapping("/photo/{photoId}")
    public ResponseEntity<?> getPhoto(@PathVariable Integer photoId) {
        try {
            PostPhoto photo = pps.findById(photoId).orElseThrow();
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photo.getPhotoData());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getPosts(Authentication auth) {
        try {
            User currentUser = auth != null ? us.findByUsername(auth.getName()) : null;
            List<Post> allPosts = ps.findAll();
            
            List<Post> visiblePosts = allPosts.stream()
                .filter(post -> {
                    if (currentUser != null && post.getUser().getUserid().equals(currentUser.getUserid())) {
                        return true;
                    }
                    
                    int privacyId = post.getPrivacyType().getPrivacyId();
                    boolean isUserPrivate = post.getUser().getIsPrivate();
                    
                    if (privacyId == 3) {
                        return false;
                    }
                    
                    if (privacyId == 1) {
                        return true;
                    }
                    
                    if (privacyId == 2) {
                        if (!isUserPrivate) {
                            return true;
                        }
                        if (currentUser != null) {
                            return followService.isFollowing(currentUser.getUserid(), post.getUser().getUserid());
                        }
                    }
                    
                    return false;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(visiblePosts.stream()
                .map(p -> Map.ofEntries(
                    Map.entry("postId", p.getPostId()),
                    Map.entry("content", p.getContent()),
                    Map.entry("createdAt", p.getCreatedAt()),
                    Map.entry("username", p.getUser().getUsername()),
                    Map.entry("userId", p.getUser().getUserid()),
                    Map.entry("profilePicture", p.getUser().getProfilePicture() != null),
                    Map.entry("isLiked", currentUser != null && pls.existsByPostAndUser(p, currentUser)),
                    Map.entry("likeCount", pls.countByPost(p)),
                    Map.entry("privacyType", p.getPrivacyType().getPrivacyId()),
                    Map.entry("commentCount", cs.countByPost(p)),
                    Map.entry("isOwner", currentUser != null && p.getUser().getUserid().equals(currentUser.getUserid())),
                    Map.entry("photos", pps.findByPostOrderByPhotoOrderAsc(p).stream()
                        .map(photo -> Map.of(
                            "photoId", photo.getPhotoId()
                        ))
                        .collect(Collectors.toList()))
                ))
                .collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestParam(required = false) String content,
            @RequestParam Integer privacyType,
            @RequestParam(required = false) List<MultipartFile> photos,
            Authentication auth) {
        try {
            if (content == null && (photos == null || photos.isEmpty())) {
                return ResponseEntity.badRequest().build();
            }

            Post p = new Post();
            p.setContent(content);
            p.setUser(us.findByUsername(auth.getName()));
            p.setPrivacyType(new PostPrivacyType() {{ setPrivacyId(privacyType); }});
            p.setCreatedAt(LocalDateTime.now());
            p = ps.save(p);

            if (photos != null && !photos.isEmpty()) {
                for (int i = 0; i < photos.size(); i++) {
                    MultipartFile photo = photos.get(i);
                    if (!photo.isEmpty()) {
                        PostPhoto postPhoto = new PostPhoto();
                        postPhoto.setPost(p);
                        postPhoto.setPhotoData(photo.getBytes());
                        postPhoto.setPhotoOrder(i);
                        postPhoto.setCreatedAt(LocalDateTime.now());
                        pps.save(postPhoto);
                    }
                }
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/like/{postId}")
    public ResponseEntity<?> toggleLike(@PathVariable Integer postId, Authentication auth) {
        try {
            Post post = ps.findById(postId).orElseThrow();
            User user = us.findByUsername(auth.getName());
            
            boolean liked = pls.toggleLike(post, user);
            long likeCount = pls.countByPost(post);
            
            return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Integer postId, Authentication auth) {
        try {
            User currentUser = us.findByUsername(auth.getName());
            Post post = ps.findById(postId).orElseThrow();
            
            if (!post.getUser().getUserid().equals(currentUser.getUserid())) {
                return ResponseEntity.status(403).body("Bu gönderiyi silme yetkiniz yok");
            }
            
            ps.delete(post);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Integer postId) {
        try {
            Post post = ps.findById(postId).orElseThrow();
            return ResponseEntity.ok(Map.ofEntries(
                Map.entry("postId", post.getPostId()),
                Map.entry("content", post.getContent()),
                Map.entry("privacyType", post.getPrivacyType().getPrivacyId()),
                Map.entry("photos", pps.findByPostOrderByPhotoOrderAsc(post).stream()
                    .map(photo -> Map.of(
                        "photoId", photo.getPhotoId()
                    ))
                    .collect(Collectors.toList()))
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/update/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable Integer postId,
            @RequestParam String content,
            @RequestParam Integer privacyType,
            @RequestParam(required = false) List<Integer> existingPhotoIds,
            @RequestParam(required = false) List<MultipartFile> photos,
            Authentication auth) {
        try {
            User currentUser = us.findByUsername(auth.getName());
            Post post = ps.findById(postId).orElseThrow();
            
            if (!post.getUser().getUserid().equals(currentUser.getUserid())) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bu gönderiyi düzenleme yetkiniz yok"));
            }
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Gönderi içeriği boş olamaz"));
            }
            
            List<PostPhoto> currentPhotos = pps.findByPostOrderByPhotoOrderAsc(post);
            if (existingPhotoIds == null) {
                existingPhotoIds = new ArrayList<>();
            }
            
            for (PostPhoto photo : currentPhotos) {
                if (!existingPhotoIds.contains(photo.getPhotoId())) {
                    pps.delete(photo);
                }
            }
            
            if (photos != null && !photos.isEmpty()) {
                int startOrder = currentPhotos.size();
                for (int i = 0; i < photos.size(); i++) {
                    MultipartFile photo = photos.get(i);
                    if (!photo.isEmpty()) {
                        PostPhoto postPhoto = new PostPhoto();
                        postPhoto.setPost(post);
                        postPhoto.setPhotoData(photo.getBytes());
                        postPhoto.setPhotoOrder(startOrder + i);
                        postPhoto.setCreatedAt(LocalDateTime.now());
                        pps.save(postPhoto);
                    }
                }
            }
            
            post.setContent(content);
            post.setPrivacyType(new PostPrivacyType() {{ setPrivacyId(privacyType); }});
            post.setUpdatedAt(LocalDateTime.now());
            ps.save(post);
            
            return ResponseEntity.ok(Map.of("success", true));
        } catch (NoSuchElementException e) {
            logger.error("Gönderi bulunamadı: postId={}", postId, e);
            return ResponseEntity.status(404)
                .body(Map.of("success", false, "message", "Gönderi bulunamadı"));
        } catch (Exception e) {
            logger.error("Gönderi güncellenirken hata oluştu: postId={}", postId, e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Gönderi güncellenirken bir hata oluştu"));
        }
    }
} 