package org.example.service;

import org.example.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class ProfilePhotoServiceImpl implements ProfilePhotoService {
    private static final Logger logger = LoggerFactory.getLogger(ProfilePhotoServiceImpl.class);
    private final Path uploadDir = Paths.get("uploads/profile-photos");
    private final UserService userService;
    
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".tiff", ".svg"
    ));
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_DIMENSION = 2048;

    @Autowired
    public ProfilePhotoServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void uploadProfilePhoto(String username, MultipartFile file) throws IOException {
        validatePhotoFile(file);
        createUploadDirectoryIfNotExists();
        
        User user = userService.findByUsername(username);
        user.setProfilePicture(file.getBytes());
        userService.updateUser(user);
        
        logger.info("Profil fotoğrafı başarıyla yüklendi. Kullanıcı: {}, Dosya: {}, Boyut: {} bytes", 
            username, file.getOriginalFilename(), file.getSize());
    }

    @Override
    public byte[] getProfilePhoto(Long userId) {
        try {
            User user = userService.findById(userId);
            return user.getProfilePicture();
        } catch (Exception e) {
            logger.error("Profil fotoğrafı alınırken hata oluştu: {}", e.getMessage(), e);
            return new byte[0];
        }
    }

    @Override
    public void validatePhotoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Lütfen bir fotoğraf seçin");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("Geçersiz dosya adı");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException(String.format("Dosya boyutu çok büyük. Maksimum boyut: %d MB", MAX_FILE_SIZE / (1024 * 1024)));
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException(String.format("Desteklenmeyen dosya formatı. İzin verilen formatlar: %s", 
                String.join(", ", ALLOWED_EXTENSIONS)));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Geçersiz dosya türü. Sadece resim dosyaları yüklenebilir.");
        }

        if (!extension.equals(".svg")) {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    throw new IOException("Geçersiz resim dosyası");
                }
                if (image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION) {
                    throw new IOException(String.format("Resim boyutları çok büyük. Maksimum boyut: %d x %d piksel", MAX_DIMENSION, MAX_DIMENSION));
                }
            } catch (IOException e) {
                throw new IOException("Resim dosyası okunamadı: " + e.getMessage());
            }
        }
    }

    private void createUploadDirectoryIfNotExists() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            logger.info("Profil fotoğrafları için dizin oluşturuldu: {}", uploadDir);
        }
    }
} 