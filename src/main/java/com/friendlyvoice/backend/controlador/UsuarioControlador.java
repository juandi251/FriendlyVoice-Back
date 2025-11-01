package com.friendlyvoice.backend.controlador;

import com.friendlyvoice.backend.dto.ActualizarPerfilDTO;
import com.friendlyvoice.backend.dto.UsuarioDTO;
import com.friendlyvoice.backend.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    // Crear usuario (upsert)
    @PostMapping("/{userId}")
    public ResponseEntity<?> crearUsuario(
            @PathVariable String userId,
            @RequestBody Map<String, Object> data) {
        try {
            UsuarioDTO usuario = usuarioServicio.crearUsuario(userId, data);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosUsuarios() {
        try {
            List<UsuarioDTO> usuarios = usuarioServicio.obtenerTodosLosUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener usuario por email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> obtenerUsuarioPorEmail(@PathVariable String email) {
        try {
            UsuarioDTO usuario = usuarioServicio.obtenerUsuarioPorEmail(email);
            
            // CRÍTICO: Verificar si la cuenta está bloqueada y devolver error si lo está
            // Esto permite que el frontend verifique el bloqueo antes del login
            if (usuario.getIsBlocked() != null && usuario.getIsBlocked()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error", "Cuenta bloqueada",
                                "message", "Tu cuenta ha sido bloqueada por demasiados intentos fallidos. Por favor, reporta este bloqueo a juandi23154@gmail.com para recibir asistencia.",
                                "isBlocked", true
                        ));
            }
            
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // CRÍTICO: Endpoint específico para verificar bloqueo antes del login
    @GetMapping("/email/{email}/bloqueada")
    public ResponseEntity<?> verificarBloqueoPorEmail(@PathVariable String email) {
        try {
            UsuarioDTO usuario = usuarioServicio.obtenerUsuarioPorEmail(email);
            boolean bloqueada = usuario.getIsBlocked() != null && usuario.getIsBlocked();
            return ResponseEntity.ok(Map.of("isBlocked", bloqueada, "message", bloqueada 
                    ? "Tu cuenta ha sido bloqueada por demasiados intentos fallidos. Por favor, reporta este bloqueo a juandi23154@gmail.com para recibir asistencia."
                    : "Cuenta activa"));
        } catch (Exception e) {
            // Si el usuario no existe, no está bloqueado
            return ResponseEntity.ok(Map.of("isBlocked", false, "message", "Usuario no encontrado"));
        }
    }

    // Obtener usuario por ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable String userId) {
        try {
            UsuarioDTO usuario = usuarioServicio.obtenerUsuarioPorId(userId);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Buscar usuarios por nombre
    @GetMapping("/buscar/{nombre}")
    public ResponseEntity<?> buscarUsuariosPorNombre(@PathVariable String nombre) {
        try {
            List<UsuarioDTO> usuarios = usuarioServicio.buscarUsuariosPorNombre(nombre);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Actualizar perfil de usuario
    @PutMapping("/{userId}")
    public ResponseEntity<?> actualizarPerfil(
            @PathVariable String userId,
            @RequestBody ActualizarPerfilDTO dto) {
        try {
            UsuarioDTO usuario = usuarioServicio.actualizarPerfil(userId, dto);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Seguir a un usuario
    @PostMapping("/{userId}/seguir/{userIdToFollow}")
    public ResponseEntity<?> seguirUsuario(
            @PathVariable String userId,
            @PathVariable String userIdToFollow) {
        try {
            usuarioServicio.seguirUsuario(userId, userIdToFollow);
            return ResponseEntity.ok(Map.of("message", "Usuario seguido exitosamente"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Dejar de seguir a un usuario
    @DeleteMapping("/{userId}/seguir/{userIdToUnfollow}")
    public ResponseEntity<?> dejarDeSeguir(
            @PathVariable String userId,
            @PathVariable String userIdToUnfollow) {
        try {
            usuarioServicio.dejarDeSeguir(userId, userIdToUnfollow);
            return ResponseEntity.ok(Map.of("message", "Dejaste de seguir al usuario"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener seguidores mutuos
    @GetMapping("/{userId}/mutuos")
    public ResponseEntity<?> obtenerSeguidoresMutuos(@PathVariable String userId) {
        try {
            List<UsuarioDTO> mutuos = usuarioServicio.obtenerSeguidoresMutuos(userId);
            return ResponseEntity.ok(mutuos);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Completar onboarding
    @PostMapping("/{userId}/onboarding")
    public ResponseEntity<?> completarOnboarding(
            @PathVariable String userId,
            @RequestBody Map<String, Object> data) {
        try {
            UsuarioDTO usuario = usuarioServicio.completarOnboarding(userId, data);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Desbloquear cuenta de usuario (solo admin)
    @PostMapping("/{userId}/desbloquear")
    public ResponseEntity<?> desbloquearCuenta(@PathVariable String userId) {
        try {
            UsuarioDTO usuario = usuarioServicio.desbloquearCuenta(userId);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Verificar si la cuenta está bloqueada
    @GetMapping("/{userId}/bloqueada")
    public ResponseEntity<?> verificarBloqueo(@PathVariable String userId) {
        try {
            boolean bloqueada = usuarioServicio.estaBloqueada(userId);
            return ResponseEntity.ok(Map.of("isLocked", bloqueada));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Incrementar intentos fallidos por email
    @PostMapping("/email/{email}/incrementar-intentos")
    public ResponseEntity<?> incrementarIntentosPorEmail(@PathVariable String email) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║ === ENDPOINT: Incrementar Intentos por Email ===            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("Email recibido: " + email);
        
        try {
            System.out.println("Llamando a usuarioServicio.incrementarIntentosLoginPorEmail(" + email + ")...");
            usuarioServicio.incrementarIntentosLoginPorEmail(email);
            System.out.println("✅ Servicio completado exitosamente - Intentos incrementados");
            return ResponseEntity.ok(Map.of("message", "Intentos incrementados", "success", true));
        } catch (RuntimeException e) {
            // CRÍTICO: Capturar RuntimeException explícitamente (puede venir de la transacción)
            System.err.println("╔══════════════════════════════════════════════════════════════╗");
            System.err.println("║ ❌ RUNTIME EXCEPTION en endpoint incrementarIntentosPorEmail  ║");
            System.err.println("╠══════════════════════════════════════════════════════════════╣");
            System.err.println("║ Email: " + email);
            System.err.println("║ Tipo: " + e.getClass().getName());
            System.err.println("║ Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("║ Causa: " + e.getCause().getClass().getName());
                System.err.println("║ Mensaje de causa: " + e.getCause().getMessage());
            }
            System.err.println("╚══════════════════════════════════════════════════════════════╝");
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al incrementar intentos: " + e.getMessage(), "success", false));
        } catch (Exception e) {
            // Capturar cualquier otra excepción (ExecutionException, InterruptedException, etc.)
            System.err.println("╔══════════════════════════════════════════════════════════════╗");
            System.err.println("║ ❌ EXCEPTION en endpoint incrementarIntentosPorEmail          ║");
            System.err.println("╠══════════════════════════════════════════════════════════════╣");
            System.err.println("║ Email: " + email);
            System.err.println("║ Tipo: " + e.getClass().getName());
            System.err.println("║ Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("║ Causa: " + e.getCause().getClass().getName());
                System.err.println("║ Mensaje de causa: " + e.getCause().getMessage());
            }
            System.err.println("╚══════════════════════════════════════════════════════════════╝");
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "success", false));
        }
    }

    // Resetear intentos de login (para pruebas y administración)
    @PostMapping("/{userId}/resetear-intentos")
    public ResponseEntity<?> resetearIntentos(@PathVariable String userId) {
        try {
            usuarioServicio.resetearIntentosLogin(userId);
            return ResponseEntity.ok(Map.of("message", "Intentos de login reseteados"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
