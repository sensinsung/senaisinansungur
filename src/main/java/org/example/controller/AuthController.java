package org.example.controller;

import org.example.entity.User;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam(required = false) Boolean rememberMe,
                       RedirectAttributes redirectAttributes) {
        try {
            logger.info("Giriş denemesi: {}", username);
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Kullanıcı başarıyla giriş yaptı: {}", username);
            
            return "redirect:/";
        } catch (Exception e) {
            logger.error("Giriş hatası: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Kullanıcı adı veya şifre hatalı");
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String username,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String confirmPassword,
                          RedirectAttributes redirectAttributes) {
        try {
            logger.info("Kayıt denemesi: {}", username);
            
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Şifreler eşleşmiyor");
            }
            
            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPasswordHash(password);
            
            User savedUser = userService.registerUser(user);
            logger.info("Kullanıcı başarıyla kaydedildi: {}", savedUser.getUserid());
            
            redirectAttributes.addFlashAttribute("success", "Kayıt başarılı! Giriş yapabilirsiniz.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            logger.error("Kayıt hatası: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
} 