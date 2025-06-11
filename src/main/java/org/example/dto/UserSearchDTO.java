package org.example.dto;

import lombok.Data;

@Data
public class UserSearchDTO {
    private Long userid;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean hasProfilePicture;
} 