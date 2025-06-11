package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.example.service.AuthenticationService;
import org.example.service.impl.AuthenticationServiceImpl;
import org.example.service.FollowService;
import org.example.service.UserService;
import org.example.service.NotificationService;
import org.example.repository.FollowRepository;
import org.example.repository.NotificationRepository;
import org.example.repository.NotificationTypeRepository;
import org.example.repository.FollowRequestRepository;

@SpringBootApplication
public class Application {
    
    @Bean
    public AuthenticationService authenticationService(UserService userService) {
        return new AuthenticationServiceImpl(userService);
    }
    
    @Bean
    public NotificationService notificationService(NotificationTypeRepository notificationTypeRepository,
                                                 NotificationRepository notificationRepository) {
        return new NotificationService(notificationTypeRepository, notificationRepository);
    }
    
    @Bean
    public FollowService followService(FollowRepository followRepository, 
                                     FollowRequestRepository followRequestRepository,
                                     UserService userService,
                                     NotificationService notificationService) {
        return new FollowService(followRepository, followRequestRepository, userService, notificationService);
    }
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
} 