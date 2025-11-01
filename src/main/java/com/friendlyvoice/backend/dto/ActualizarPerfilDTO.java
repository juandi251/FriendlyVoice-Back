package com.friendlyvoice.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilDTO {
    private String name;
    private String bio;
    private String avatarUrl;
    private List<String> interests;
    private List<String> hobbies;
    private String bioSoundUrl;
    private Integer loginAttempts; // Para resetear intentos de login
}
