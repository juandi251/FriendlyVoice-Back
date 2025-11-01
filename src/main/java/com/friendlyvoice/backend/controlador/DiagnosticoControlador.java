package com.friendlyvoice.backend.controlador;

import com.google.firebase.FirebaseApp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostico")
public class DiagnosticoControlador {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("message", "Backend is running");
        return ResponseEntity.ok(health);
    }

    @GetMapping(value = {"/", ""})
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Diagnóstico API disponible");
        response.put("endpoints", "/health, /firebase");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/firebase")
    public ResponseEntity<Map<String, Object>> diagnosticarFirebase() {
        Map<String, Object> diagnostico = new HashMap<>();
        
        // Verificar variable de entorno
        String firebaseServiceAccount = System.getenv("FIREBASE_SERVICE_ACCOUNT");
        boolean variableExiste = firebaseServiceAccount != null;
        boolean variableVacia = variableExiste && firebaseServiceAccount.isEmpty();
        
        diagnostico.put("variableEntorno", Map.of(
            "existe", variableExiste,
            "estaVacia", variableVacia,
            "tieneContenido", variableExiste && !variableVacia,
            "tamaño", variableExiste ? firebaseServiceAccount.length() : 0
        ));
        
        // Verificar Firebase App
        boolean firebaseInicializado = !FirebaseApp.getApps().isEmpty();
        diagnostico.put("firebase", Map.of(
            "inicializado", firebaseInicializado,
            "appsCount", FirebaseApp.getApps().size()
        ));
        
        // Verificar otras variables importantes
        String port = System.getenv("PORT");
        String corsOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
        
        diagnostico.put("variablesSistema", Map.of(
            "PORT", port != null ? port : "NO CONFIGURADA",
            "CORS_ALLOWED_ORIGINS", corsOrigins != null ? corsOrigins : "NO CONFIGURADA"
        ));
        
        // Estado general
        String estado;
        String mensaje;
        if (!variableExiste) {
            estado = "ERROR";
            mensaje = "Variable de entorno FIREBASE_SERVICE_ACCOUNT no configurada. Configurela en Render → Environment.";
        } else if (variableVacia) {
            estado = "ERROR";
            mensaje = "Variable de entorno FIREBASE_SERVICE_ACCOUNT está vacía.";
        } else if (!firebaseInicializado) {
            estado = "ERROR";
            mensaje = "Firebase no se inicializó correctamente. Revise los logs para más detalles.";
        } else {
            estado = "OK";
            mensaje = "Firebase configurado correctamente.";
        }
        
        diagnostico.put("estado", estado);
        diagnostico.put("mensaje", mensaje);
        
        return ResponseEntity.ok(diagnostico);
    }
}

