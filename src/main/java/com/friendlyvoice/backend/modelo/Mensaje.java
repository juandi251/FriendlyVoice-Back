package com.friendlyvoice.backend.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {
    private String id;
    private String chatId;
    private String senderId;
    private String recipientId;
    private String voiceUrl;
    private String createdAt;
    private Boolean isRead;
}
