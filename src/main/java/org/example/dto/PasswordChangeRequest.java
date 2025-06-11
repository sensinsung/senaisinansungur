package org.example.dto;

public class PasswordChangeRequest {
    private final String currentPassword;
    private final String newPassword;
    private final String confirmPassword;

    public PasswordChangeRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void validate() {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Yeni şifreler eşleşmiyor");
        }
    }
} 