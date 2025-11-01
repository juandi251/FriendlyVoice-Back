package com.friendlyvoice.backend.controlador;

import com.friendlyvoice.backend.dto.MensajeDTO;
import com.friendlyvoice.backend.modelo.Mensaje;
import com.friendlyvoice.backend.servicio.MensajeServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeControlador {

    @Autowired
    private MensajeServicio mensajeServicio;

    // Enviar mensaje directo
    @PostMapping("/enviar/{senderId}")
    public ResponseEntity<?> enviarMensaje(
            @PathVariable String senderId,
            @RequestBody MensajeDTO dto) {
        try {
            Mensaje mensaje = mensajeServicio.enviarMensaje(
                    senderId,
                    dto.getRecipientId(),
                    dto.getVoiceUrl()
            );
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(mensaje);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener mensajes de un chat
    @GetMapping("/chat/{userId}/{chatPartnerId}")
    public ResponseEntity<?> obtenerMensajesDeChat(
            @PathVariable String userId,
            @PathVariable String chatPartnerId) {
        try {
            List<Mensaje> mensajes = mensajeServicio.obtenerMensajesDeChat(userId, chatPartnerId);
            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Marcar mensaje como leído
    @PutMapping("/{messageId}/leido")
    public ResponseEntity<?> marcarComoLeido(@PathVariable String messageId) {
        try {
            mensajeServicio.marcarComoLeido(messageId);
            return ResponseEntity.ok(Map.of("message", "Mensaje marcado como leído"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener mensajes no leídos
    @GetMapping("/no-leidos/{userId}")
    public ResponseEntity<?> obtenerMensajesNoLeidos(@PathVariable String userId) {
        try {
            List<Mensaje> mensajes = mensajeServicio.obtenerMensajesNoLeidos(userId);
            return ResponseEntity.ok(mensajes);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener resumen de un chat (último mensaje y contador sin leer) - OPTIMIZADO
    @GetMapping("/resumen/{userId}/{chatPartnerId}")
    public ResponseEntity<?> obtenerResumenChat(
            @PathVariable String userId,
            @PathVariable String chatPartnerId) {
        try {
            Map<String, Object> resumen = mensajeServicio.obtenerResumenChat(userId, chatPartnerId);
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
