package org.example.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface ProfilePhotoService {
    void uploadProfilePhoto(String username, MultipartFile file) throws IOException;
    byte[] getProfilePhoto(Long userId);
    void validatePhotoFile(MultipartFile file) throws IOException;
} 