package com.friendlyvoice.backend.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private String id;
    private String email;
    private String name;
    private Boolean emailVerified;
    private String avatarUrl;
    private String bio;
    private List<String> followers = new ArrayList<>();
    private List<String> following = new ArrayList<>();
    private List<String> interests = new ArrayList<>();
    private List<String> personalityTags = new ArrayList<>();
    private String bioSoundUrl;
    private String dateOfBirth;
    private List<String> hobbies = new ArrayList<>();
    private Boolean onboardingComplete;
    private String role; // "admin" or "user"
    private Integer loginAttempts = 0; // Contador de intentos fallidos
    private Boolean isLocked = false; // Estado de bloqueo de cuenta
}
