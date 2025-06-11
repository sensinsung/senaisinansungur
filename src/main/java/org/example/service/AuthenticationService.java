package org.example.service;

import org.example.entity.User;

public interface AuthenticationService {
    User getCurrentUser();
    String getCurrentUsername();
} 