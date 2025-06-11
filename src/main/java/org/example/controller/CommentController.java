package org.example.controller;

import org.example.entity.Comment;
import org.example.entity.Post;
import org.example.entity.User;
import org.example.entity.CommentReply;
import org.example.service.CommentService;
import org.example.service.PostService;
import org.example.service.UserService;
import org.example.service.CommentLikeService;
import org.example.service.CommentReplyService;
import org.example.service.ReplyLikeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/comment")
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    
    private final CommentService commentService;
    private final PostService postService;
    private final UserService userService;
    private final CommentLikeService commentLikeService;
    private final CommentReplyService commentReplyService;
    private final ReplyLikeService replyLikeService;

    public CommentController(CommentService commentService,
                           PostService postService,
                           UserService userService,
                           CommentLikeService commentLikeService,
                           CommentReplyService commentReplyService,
                           ReplyLikeService replyLikeService) {
        this.commentService = commentService;
        this.postService = postService;
        this.userService = userService;
        this.commentLikeService = commentLikeService;
        this.commentReplyService = commentReplyService;
        this.replyLikeService = replyLikeService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createComment(@RequestBody Map<String, String> payload, Authentication auth) {
        try {
            String content = payload.get("content");
            Integer postId = Integer.parseInt(payload.get("postId"));
            
            Post post = postService.findById(postId).orElseThrow();
            User user = userService.findByUsername(auth.getName());
            
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setPost(post);
            comment.setUser(user);
            comment.setCreatedAt(LocalDateTime.now());
            
            commentService.save(comment);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "commentCount", commentService.countByPost(post)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/list/{postId}")
    public ResponseEntity<?> getComments(@PathVariable Integer postId, Authentication auth) {
        try {
            User currentUser = auth != null ? userService.findByUsername(auth.getName()) : null;
            Post post = postService.findById(postId).orElseThrow();
            
            return ResponseEntity.ok(commentService.findByPostOrderByCreatedAtDesc(post).stream()
                .<Map<String, Object>>map(c -> Map.ofEntries(
                    Map.entry("commentId", c.getCommentId()),
                    Map.entry("content", c.getContent()),
                    Map.entry("createdAt", c.getCreatedAt()),
                    Map.entry("username", c.getUser().getUsername()),
                    Map.entry("userId", c.getUser().getUserid()),
                    Map.entry("profilePicture", c.getUser().getProfilePicture() != null),
                    Map.entry("isLiked", currentUser != null && commentLikeService.existsByCommentAndUser(c, currentUser)),
                    Map.entry("likeCount", commentLikeService.countByComment(c)),
                    Map.entry("replyCount", commentReplyService.countByComment(c)),
                    Map.entry("isOwner", currentUser != null && c.getUser().getUserid().equals(currentUser.getUserid()))
                ))
                .collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/like/{commentId}")
    public ResponseEntity<?> toggleLike(@PathVariable Integer commentId, Authentication auth) {
        try {
            Comment comment = commentService.findById(commentId).orElseThrow();
            User user = userService.findByUsername(auth.getName());
            
            boolean liked = commentLikeService.toggleLike(comment, user);
            long likeCount = commentLikeService.countByComment(comment);
            
            return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/reply/{commentId}")
    public ResponseEntity<?> createReply(@PathVariable Integer commentId, @RequestBody Map<String, String> payload, Authentication auth) {
        try {
            String content = payload.get("content");
            Comment comment = commentService.findById(commentId).orElseThrow();
            User user = userService.findByUsername(auth.getName());
            
            CommentReply reply = new CommentReply();
            reply.setContent(content);
            reply.setComment(comment);
            reply.setUser(user);
            reply.setCreatedAt(LocalDateTime.now());
            
            commentReplyService.save(reply);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "replyCount", commentReplyService.countByComment(comment)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/replies/{commentId}")
    public ResponseEntity<?> getReplies(@PathVariable Integer commentId, Authentication auth) {
        try {
            Comment comment = commentService.findById(commentId).orElseThrow();
            User currentUser = auth != null ? userService.findByUsername(auth.getName()) : null;
            List<CommentReply> replies = commentReplyService.findByCommentOrderByCreatedAtDesc(comment);
            
            List<Map<String, Object>> response = replies.stream()
                .<Map<String, Object>>map(r -> Map.of(
                    "replyId", r.getReplyId(),
                    "content", r.getContent(),
                    "createdAt", r.getCreatedAt(),
                    "username", r.getUser().getUsername(),
                    "userId", r.getUser().getUserid(),
                    "profilePicture", r.getUser().getProfilePicture() != null,
                    "isLiked", currentUser != null && replyLikeService.existsByReplyAndUser(r, currentUser),
                    "likeCount", replyLikeService.countByReply(r),
                    "isOwner", currentUser != null && r.getUser().getUserid().equals(currentUser.getUserid())
                ))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            logger.error("Yorum bulunamadı: commentId={}", commentId, e);
            return ResponseEntity.status(404)
                .body(Map.of("success", false, "message", "Yorum bulunamadı"));
        } catch (Exception e) {
            logger.error("Yanıtlar yüklenirken hata oluştu: commentId={}", commentId, e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Yanıtlar yüklenirken bir hata oluştu"));
        }
    }

    @PostMapping("/reply/like/{replyId}")
    public ResponseEntity<?> toggleReplyLike(@PathVariable Integer replyId, Authentication auth) {
        try {
            CommentReply reply = commentReplyService.findById(replyId).orElseThrow();
            User user = userService.findByUsername(auth.getName());
            
            boolean liked = replyLikeService.toggleLike(reply, user);
            long likeCount = replyLikeService.countByReply(reply);
            
            return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId, Authentication auth) {
        try {
            User currentUser = userService.findByUsername(auth.getName());
            Comment comment = commentService.findById(commentId).orElseThrow();
            
            if (!comment.getUser().getUserid().equals(currentUser.getUserid())) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bu yorumu silme yetkiniz yok"));
            }
            
            commentService.delete(comment);
            
            long commentCount = commentService.countByPost(comment.getPost());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "commentCount", commentCount
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                .body(Map.of("success", false, "message", "Yorum bulunamadı"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Yorum silinirken bir hata oluştu"));
        }
    }

    @PostMapping("/reply/delete/{replyId}")
    public ResponseEntity<?> deleteReply(@PathVariable Integer replyId, Authentication auth) {
        try {
            User currentUser = userService.findByUsername(auth.getName());
            CommentReply reply = commentReplyService.findById(replyId).orElseThrow();
            
            if (!reply.getUser().getUserid().equals(currentUser.getUserid())) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Bu yanıtı silme yetkiniz yok"));
            }
            
            replyLikeService.deleteByReply(reply);
            
            Integer commentId = reply.getComment().getCommentId();
            
            commentReplyService.delete(reply);
            
            long replyCount = commentReplyService.countByComment(reply.getComment());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "replyCount", replyCount,
                "commentId", commentId
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404)
                .body(Map.of("success", false, "message", "Yanıt bulunamadı"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Yanıt silinirken bir hata oluştu"));
        }
    }
} 