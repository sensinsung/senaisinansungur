package org.example.service;

import org.example.dto.PasswordChangeRequest;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPasswordHash(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Transactional
    public User registerUser(User user) {
        logger.info("Kullanıcı kaydı başlatılıyor: {}", user.getUsername());
        
        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Kullanıcı adı zaten kullanılıyor: {}", user.getUsername());
            throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn("E-posta adresi zaten kullanılıyor: {}", user.getEmail());
            throw new RuntimeException("Bu e-posta adresi zaten kullanılıyor");
        }

        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);
        
        User savedUser = userRepository.save(user);
        logger.info("Kullanıcı başarıyla kaydedildi. ID: {}", savedUser.getUserid());
        
        return savedUser;
    }

    public User findByUsername(String username) {
        logger.info("Kullanıcı aranıyor: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Kullanıcı bulunamadı: {}", username);
                    return new RuntimeException("Kullanıcı bulunamadı");
                });
    }

    public User findById(Long id) {
        logger.info("ID ile kullanıcı aranıyor: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("ID ile kullanıcı bulunamadı: {}", id);
                    return new RuntimeException("Kullanıcı bulunamadı");
                });
    }

    public void updateUser(User user) {
        if (user.getPasswordHash() != null && !user.getPasswordHash().startsWith("$2a$")) {
            String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
            user.setPasswordHash(hashedPassword);
        }
        userRepository.save(user);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        logger.info("Kullanıcı şifresi değiştiriliyor: {}", username);
        
        request.validate();
        User user = findByUsername(username);
        
        if (!checkPassword(user, request.getCurrentPassword())) {
            throw new RuntimeException("Mevcut şifre hatalı");
        }
        
        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);
        
        logger.info("Kullanıcı şifresi başarıyla değiştirildi: {}", username);
    }

    public List<User> searchUsers(String query) {
        logger.info("Kullanıcı araması yapılıyor: {}", query);
        return userRepository.searchUsers(query);
    }
} 