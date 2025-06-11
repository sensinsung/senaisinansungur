package org.example.controller;

import org.example.dto.PasswordChangeRequest;
import org.example.dto.UserSearchDTO;
import org.example.entity.User;
import org.example.service.AuthenticationService;
import org.example.service.FollowService;
import org.example.service.ProfilePhotoService;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final ProfilePhotoService profilePhotoService;
    private final AuthenticationService authenticationService;
    private final FollowService followService;

    @Autowired
    public UserController(UserService userService, 
                         ProfilePhotoService profilePhotoService,
                         AuthenticationService authenticationService,
                         FollowService followService) {
        this.userService = userService;
        this.profilePhotoService = profilePhotoService;
        this.authenticationService = authenticationService;
        this.followService = followService;
    }

    @GetMapping("/profil")
    public String profilePage(Model model) {
        try {
            User user = authenticationService.getCurrentUser();
            model.addAttribute("user", user);
            model.addAttribute("currentUser", user);
            model.addAttribute("followerCount", followService.getFollowers(user.getUserid()).size());
            model.addAttribute("followingCount", followService.getFollowing(user.getUserid()).size());
            model.addAttribute("isFollowing", false);
            return "profile";
        } catch (Exception e) {
            logger.error("Profil sayfası yüklenirken hata oluştu: {}", e.getMessage(), e);
            return "redirect:/";
        }
    }

    @GetMapping("/profil/{username}")
    public String userProfilePage(@PathVariable String username, Model model) {
        try {
            User profileUser = userService.findByUsername(username);
            User currentUser = authenticationService.getCurrentUser();
            
            model.addAttribute("user", profileUser);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("followerCount", followService.getFollowers(profileUser.getUserid()).size());
            model.addAttribute("followingCount", followService.getFollowing(profileUser.getUserid()).size());
            model.addAttribute("isFollowing", followService.isFollowing(currentUser.getUserid(), profileUser.getUserid()));
            
            return "profile";
        } catch (Exception e) {
            logger.error("Profil sayfası yüklenirken hata oluştu: {}", e.getMessage(), e);
            return "redirect:/";
        }
    }

    @GetMapping("/ayarlar")
    public String settingsPage(Model model) {
        try {
            User user = authenticationService.getCurrentUser();
            model.addAttribute("user", user);
            return "settings";
        } catch (Exception e) {
            logger.error("Ayarlar sayfası yüklenirken hata oluştu: {}", e.getMessage(), e);
            return "redirect:/";
        }
    }

    @PostMapping("/ayarlar/foto-yukle")
    public String uploadProfilePhoto(@RequestParam("profilePhoto") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = authenticationService.getCurrentUsername();
            profilePhotoService.uploadProfilePhoto(username, file);
            redirectAttributes.addFlashAttribute("success", "Profil fotoğrafı başarıyla güncellendi");
        } catch (Exception e) {
            logger.error("Profil fotoğrafı yüklenirken hata oluştu: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ayarlar";
    }

    @PostMapping("/ayarlar/sifre-degistir")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            PasswordChangeRequest request = new PasswordChangeRequest(currentPassword, newPassword, confirmPassword);
            String username = authenticationService.getCurrentUsername();
            userService.changePassword(username, request);
            redirectAttributes.addFlashAttribute("success", "Şifreniz başarıyla güncellendi");
        } catch (Exception e) {
            logger.error("Şifre değiştirilirken hata oluştu: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ayarlar";
    }

    @PostMapping("/ayarlar/gizlilik-ayarla")
    public String updatePrivacySettings(@RequestParam(required = false) Boolean isPrivate,
                                      RedirectAttributes redirectAttributes) {
        try {
            User user = authenticationService.getCurrentUser();
            user.setIsPrivate(isPrivate != null && isPrivate);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Profil gizlilik ayarları güncellendi");
        } catch (Exception e) {
            logger.error("Gizlilik ayarları güncellenirken hata oluştu: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ayarlar";
    }

    @GetMapping("/uploads/profile-photos/{userid}")
    @ResponseBody
    public byte[] getProfilePhoto(@PathVariable Long userid) {
        return profilePhotoService.getProfilePhoto(userid);
    }

    @GetMapping("/api/users/search")
    @ResponseBody
    public List<UserSearchDTO> searchUsers(@RequestParam String query) {
        List<User> users = userService.searchUsers(query);
        return users.stream()
            .map(user -> {
                UserSearchDTO dto = new UserSearchDTO();
                dto.setUserid(user.getUserid());
                dto.setFirstName(user.getFirstName());
                dto.setLastName(user.getLastName());
                dto.setUsername(user.getUsername());
                dto.setHasProfilePicture(user.getProfilePicture() != null);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @GetMapping("/arama")
    public String searchPage(@RequestParam(required = false) String query, Model model) {
        if (query != null && !query.trim().isEmpty()) {
            model.addAttribute("users", userService.searchUsers(query));
            model.addAttribute("searchQuery", query);
        }
        return "search";
    }
} 