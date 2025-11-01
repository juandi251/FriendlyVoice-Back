package com.friendlyvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private String id;
    private String email;
    private String name;
    private String avatarUrl;
    private String bio;
    private List<String> followers;
    private List<String> following;
    private List<String> interests;
    private List<String> hobbies;
    private String bioSoundUrl;
    private Boolean onboardingComplete;
    private String role;
    private Boolean isBlocked; // Estado de bloqueo de cuenta
    private Integer loginAttempts; // Número de intentos fallidos de login
}
