package com.friendlyvoice.backend.servicio;

import com.friendlyvoice.backend.dto.ActualizarPerfilDTO;
import com.friendlyvoice.backend.dto.UsuarioDTO;
import com.friendlyvoice.backend.modelo.Usuario;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UsuarioServicio {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "users";

    // Obtener todos los usuarios
    public List<UsuarioDTO> obtenerTodosLosUsuarios() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        return documents.stream()
                .map(this::convertirAUsuarioDTO)
                .collect(Collectors.toList());
    }

    // Obtener usuario por email
    public UsuarioDTO obtenerUsuarioPorEmail(String email) throws ExecutionException, InterruptedException {
        System.out.println("=== OBTENER USUARIO POR EMAIL ===");
        System.out.println("Email: " + email);
        System.out.println("Colección: " + COLLECTION_NAME);
        
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .limit(1)
                .get();
        
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        if (documents.isEmpty()) {
            System.out.println("❌ Usuario no encontrado con email: " + email);
            throw new RuntimeException("Usuario no encontrado con email: " + email);
        }
        
        QueryDocumentSnapshot doc = documents.get(0);
        String userId = doc.getId();
        Boolean isBlockedFromFirestore = doc.getBoolean("isBlocked");
        
        System.out.println("Usuario encontrado:");
        System.out.println("  - ID: " + userId);
        System.out.println("  - Email: " + doc.getString("email"));
        System.out.println("  - isBlocked en Firestore (RAW): " + isBlockedFromFirestore);
        System.out.println("  - Tipo de isBlocked: " + (isBlockedFromFirestore != null ? isBlockedFromFirestore.getClass().getName() : "null"));
        
        UsuarioDTO dto = convertirAUsuarioDTO(doc);
        
        System.out.println("DTO convertido:");
        System.out.println("  - ID: " + dto.getId());
        System.out.println("  - Email: " + dto.getEmail());
        System.out.println("  - isBlocked en DTO: " + dto.getIsBlocked());
        System.out.println("  - Tipo de isBlocked en DTO: " + (dto.getIsBlocked() != null ? dto.getIsBlocked().getClass().getName() : "null"));
        
        // CRÍTICO: Verificar que isBlocked se lea correctamente
        if (isBlockedFromFirestore != null && isBlockedFromFirestore && !dto.getIsBlocked()) {
            System.err.println("⚠⚠⚠ ERROR: isBlocked es true en Firestore pero false en DTO!");
            System.err.println("  - Firestore: " + isBlockedFromFirestore);
            System.err.println("  - DTO: " + dto.getIsBlocked());
            // Forzar el valor correcto
            dto.setIsBlocked(true);
            System.err.println("  - DTO corregido a: " + dto.getIsBlocked());
        }
        
        System.out.println("=== FIN OBTENER USUARIO POR EMAIL ===");
        return dto;
    }

    // Obtener usuario por ID
    public UsuarioDTO obtenerUsuarioPorId(String userId) throws ExecutionException, InterruptedException {
        System.out.println("=== OBTENER USUARIO POR ID ===");
        System.out.println("Usuario ID: " + userId);
        System.out.println("Colección: " + COLLECTION_NAME);
        
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Boolean isBlockedFromFirestore = document.getBoolean("isBlocked");
            
            System.out.println("Usuario encontrado:");
            System.out.println("  - ID: " + document.getId());
            System.out.println("  - Email: " + document.getString("email"));
            System.out.println("  - isBlocked en Firestore (RAW): " + isBlockedFromFirestore);
            System.out.println("  - Tipo de isBlocked: " + (isBlockedFromFirestore != null ? isBlockedFromFirestore.getClass().getName() : "null"));
            
            UsuarioDTO dto = convertirAUsuarioDTO(document);
            
            System.out.println("DTO convertido:");
            System.out.println("  - ID: " + dto.getId());
            System.out.println("  - Email: " + dto.getEmail());
            System.out.println("  - isBlocked en DTO: " + dto.getIsBlocked());
            System.out.println("  - Tipo de isBlocked en DTO: " + (dto.getIsBlocked() != null ? dto.getIsBlocked().getClass().getName() : "null"));
            
            // CRÍTICO: Verificar que isBlocked se lea correctamente
            if (isBlockedFromFirestore != null && isBlockedFromFirestore && !dto.getIsBlocked()) {
                System.err.println("⚠⚠⚠ ERROR: isBlocked es true en Firestore pero false en DTO!");
                System.err.println("  - Firestore: " + isBlockedFromFirestore);
                System.err.println("  - DTO: " + dto.getIsBlocked());
                // Forzar el valor correcto
                dto.setIsBlocked(true);
                System.err.println("  - DTO corregido a: " + dto.getIsBlocked());
            }
            
            System.out.println("=== FIN OBTENER USUARIO POR ID ===");
            return dto;
        } else {
            System.out.println("❌ Usuario no encontrado con ID: " + userId);
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
    }

    // Crear usuario (upsert simple con set/merge)
    public UsuarioDTO crearUsuario(String userId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        Map<String, Object> payload = new HashMap<>(data);
        // Asegurar campos mínimos
        payload.putIfAbsent("onboardingComplete", false);
        payload.putIfAbsent("followers", new ArrayList<>());
        payload.putIfAbsent("following", new ArrayList<>());
        // IMPORTANTE: Inicializar loginAttempts a 0 e isBlocked a false para usuarios nuevos
        payload.putIfAbsent("loginAttempts", 0);
        payload.putIfAbsent("isBlocked", false);
        ApiFuture<WriteResult> future = docRef.set(payload, SetOptions.merge());
        future.get();
        return obtenerUsuarioPorId(userId);
    }

    // Buscar usuarios por nombre
    public List<UsuarioDTO> buscarUsuariosPorNombre(String nombre) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("name", nombre)
                .whereLessThanOrEqualTo("name", nombre + "\uf8ff")
                .get();
        
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        return documents.stream()
                .map(this::convertirAUsuarioDTO)
                .collect(Collectors.toList());
    }

    // Actualizar perfil de usuario
    public UsuarioDTO actualizarPerfil(String userId, ActualizarPerfilDTO dto) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        
        Map<String, Object> updates = new HashMap<>();
        if (dto.getName() != null) updates.put("name", dto.getName());
        if (dto.getBio() != null) updates.put("bio", dto.getBio());
        if (dto.getAvatarUrl() != null) updates.put("avatarUrl", dto.getAvatarUrl());
        if (dto.getInterests() != null) updates.put("interests", dto.getInterests());
        if (dto.getHobbies() != null) updates.put("hobbies", dto.getHobbies());
        if (dto.getBioSoundUrl() != null) updates.put("bioSoundUrl", dto.getBioSoundUrl());
        
        // IMPORTANTE: Solo actualizar loginAttempts si la cuenta NO está bloqueada
        // Si la cuenta está bloqueada, no se puede resetear intentos a través de actualizarPerfil
        if (dto.getLoginAttempts() != null) {
            DocumentSnapshot currentDoc = docRef.get().get();
            if (currentDoc.exists()) {
                Boolean isBlocked = currentDoc.getBoolean("isBlocked");
                // Solo resetear intentos si NO está bloqueado
                if (isBlocked == null || !isBlocked) {
                    updates.put("loginAttempts", dto.getLoginAttempts());
                } else {
                    System.out.println("Intento de actualizar loginAttempts en cuenta bloqueada - IGNORADO");
                }
            }
        }
        
        // IMPORTANTE: Nunca permitir actualizar isBlocked a través de actualizarPerfil
        // Solo el admin puede desbloquear cuentas a través del endpoint específico
        
        // Evitar llamar update con mapa vacío
        if (!updates.isEmpty()) {
            ApiFuture<WriteResult> future = docRef.update(updates);
            future.get(); // Wait for the update to complete
        }

        return obtenerUsuarioPorId(userId);
    }

    // Seguir a un usuario
    public void seguirUsuario(String userId, String userIdToFollow) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection(COLLECTION_NAME).document(userId);
        DocumentReference targetRef = firestore.collection(COLLECTION_NAME).document(userIdToFollow);

        // Obtener datos actuales
        ApiFuture<DocumentSnapshot> userFuture = userRef.get();
        ApiFuture<DocumentSnapshot> targetFuture = targetRef.get();

        DocumentSnapshot userDoc = userFuture.get();
        DocumentSnapshot targetDoc = targetFuture.get();

        if (!userDoc.exists() || !targetDoc.exists()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Actualizar following del usuario actual
        List<String> following = (List<String>) userDoc.get("following");
        if (following == null) following = new ArrayList<>();
        if (!following.contains(userIdToFollow)) {
            following.add(userIdToFollow);
            userRef.update("following", following).get();
        }

        // Actualizar followers del usuario objetivo
        List<String> followers = (List<String>) targetDoc.get("followers");
        if (followers == null) followers = new ArrayList<>();
        if (!followers.contains(userId)) {
            followers.add(userId);
            targetRef.update("followers", followers).get();
        }
    }

    // Dejar de seguir a un usuario
    public void dejarDeSeguir(String userId, String userIdToUnfollow) throws ExecutionException, InterruptedException {
        DocumentReference userRef = firestore.collection(COLLECTION_NAME).document(userId);
        DocumentReference targetRef = firestore.collection(COLLECTION_NAME).document(userIdToUnfollow);

        // Obtener datos actuales
        ApiFuture<DocumentSnapshot> userFuture = userRef.get();
        ApiFuture<DocumentSnapshot> targetFuture = targetRef.get();

        DocumentSnapshot userDoc = userFuture.get();
        DocumentSnapshot targetDoc = targetFuture.get();

        if (!userDoc.exists() || !targetDoc.exists()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Actualizar following del usuario actual
        List<String> following = (List<String>) userDoc.get("following");
        if (following != null) {
            following.remove(userIdToUnfollow);
            userRef.update("following", following).get();
        }

        // Actualizar followers del usuario objetivo
        List<String> followers = (List<String>) targetDoc.get("followers");
        if (followers != null) {
            followers.remove(userId);
            targetRef.update("followers", followers).get();
        }
    }

    // Obtener seguidores mutuos
    public List<UsuarioDTO> obtenerSeguidoresMutuos(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot userDoc = firestore.collection(COLLECTION_NAME).document(userId).get().get();
        
        if (!userDoc.exists()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        List<String> following = (List<String>) userDoc.get("following");
        List<String> followers = (List<String>) userDoc.get("followers");

        if (following == null || followers == null) {
            return new ArrayList<>();
        }

        // Encontrar IDs mutuos
        Set<String> mutualIds = new HashSet<>(following);
        mutualIds.retainAll(followers);

        // Obtener usuarios mutuos
        List<UsuarioDTO> mutualUsers = new ArrayList<>();
        for (String mutualId : mutualIds) {
            try {
                mutualUsers.add(obtenerUsuarioPorId(mutualId));
            } catch (Exception e) {
                // Skip if user not found
            }
        }

        return mutualUsers;
    }

    // Completar onboarding
    public UsuarioDTO completarOnboarding(String userId, Map<String, Object> data) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        
        Map<String, Object> updates = new HashMap<>(data);
        updates.put("onboardingComplete", true);

        ApiFuture<WriteResult> future = docRef.update(updates);
        future.get();

        return obtenerUsuarioPorId(userId);
    }

    // Incrementar intentos fallidos de login y bloquear si llega a 3
    // CRÍTICO: Usa TRANSACCIÓN de Firestore para garantizar atomicidad y evitar race conditions
    public void incrementarIntentosLogin(String userId) throws ExecutionException, InterruptedException {
        final DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);

        // Usa una transacción para asegurar que la lectura y escritura son atómicas
        ApiFuture<Void> transactionFuture = firestore.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(docRef).get(); // Lectura dentro de la transacción

            if (!doc.exists()) {
                throw new RuntimeException("Usuario no encontrado con ID: " + userId);
            }

            // Obtener intentos actuales
            Long loginAttemptsLong = doc.getLong("loginAttempts");
            Integer intentosActuales = (loginAttemptsLong != null) ? loginAttemptsLong.intValue() : 0;
            Boolean isBlocked = doc.getBoolean("isBlocked");

            // 1. VERIFICAR BLOQUEO
            if (isBlocked != null && isBlocked) {
                System.out.println("Usuario ya está bloqueado, ignorando intento.");
                return null; // Salir de la transacción sin error
            }

            // 2. INCREMENTAR
            Integer intentosNuevos = intentosActuales + 1;
            Map<String, Object> updates = new HashMap<>();
            updates.put("loginAttempts", intentosNuevos);

            // 3. VERIFICAR BLOQUEO POR LÍMITE (3 INTENTOS)
            // Usar >= para mayor seguridad, aunque el caso más común será == 3
            if (intentosNuevos >= 3) {
                updates.put("isBlocked", true);
                System.out.println("⚠⚠⚠ BLOQUEANDO USUARIO (Transacción): intentos >= 3. isBlocked = true");
            }
            
            // 4. ESCRITURA ATÓMICA dentro de la transacción
            transaction.update(docRef, updates); // Escritura dentro de la transacción
            
            System.out.println("✓ Intentos de login actualizados (Transacción): " + intentosActuales + " -> " + intentosNuevos);
            if (intentosNuevos >= 3) {
                System.out.println("✓ Usuario bloqueado automáticamente (isBlocked = true)");
            }
            
            return null; // Transacción exitosa
        });

        // Esperar a que la transacción termine
        try {
            System.out.println("⏳ Esperando que la transacción termine...");
            transactionFuture.get(); // Esto lanza ExecutionException si la transacción falla
            System.out.println("✅ Transacción completada exitosamente");
        } catch (ExecutionException e) {
            System.err.println("╔══════════════════════════════════════════════════════════════╗");
            System.err.println("║ ❌ ERROR FATAL EN TRANSACCIÓN                                ║");
            System.err.println("╠══════════════════════════════════════════════════════════════╣");
            System.err.println("║ Usuario ID: " + userId);
            System.err.println("║ Tipo de excepción: " + e.getClass().getName());
            System.err.println("║ Mensaje: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("║ Causa: " + e.getCause().getClass().getName());
                System.err.println("║ Mensaje de causa: " + e.getCause().getMessage());
                System.err.println("║ Stack trace de causa:");
                e.getCause().printStackTrace();
            } else {
                System.err.println("║ Causa: N/A");
            }
            System.err.println("╚══════════════════════════════════════════════════════════════╝");
            System.err.println("Stack trace completo de ExecutionException:");
            e.printStackTrace();
            
            // CRÍTICO: Re-lanzar la excepción para que el controlador la capture
            // NO capturar silenciosamente - el controlador debe saber que falló
            throw new RuntimeException("Error crítico en transacción de Firestore para usuario " + userId + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            System.err.println("╔══════════════════════════════════════════════════════════════╗");
            System.err.println("║ ❌ TRANSACCIÓN INTERRUMPIDA                                  ║");
            System.err.println("╠══════════════════════════════════════════════════════════════╣");
            System.err.println("║ Usuario ID: " + userId);
            System.err.println("║ Mensaje: " + e.getMessage());
            System.err.println("╚══════════════════════════════════════════════════════════════╝");
            e.printStackTrace();
            
            // Restaurar el estado de interrupción
            Thread.currentThread().interrupt();
            
            // CRÍTICO: Re-lanzar la excepción para que el controlador la capture
            throw new RuntimeException("Transacción interrumpida para usuario " + userId + ": " + e.getMessage(), e);
        }
    }
    
    // Incrementar intentos fallidos de login por email
    public void incrementarIntentosLoginPorEmail(String email) throws ExecutionException, InterruptedException {
        System.out.println("=== INCREMENTAR INTENTOS POR EMAIL ===");
        System.out.println("Email: " + email);
        
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get();
            
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            if (documents.isEmpty()) {
                // Si el usuario no existe en Firestore, no hacer nada (puede ser que aún no se haya creado)
                System.out.println("⚠ Usuario no encontrado con email: " + email + ". Ignorando intento.");
                return;
            }
            
            String userId = documents.get(0).getId();
            System.out.println("Usuario encontrado - ID: " + userId);
            System.out.println("Llamando a incrementarIntentosLogin(userId)...");
            
            // CRÍTICO: Llamar a incrementarIntentosLogin y propagar cualquier excepción
            incrementarIntentosLogin(userId);
            
            System.out.println("✓ incrementarIntentosLogin completado exitosamente");
            System.out.println("=== FIN INCREMENTAR INTENTOS POR EMAIL ===");
        } catch (RuntimeException e) {
            // CRÍTICO: Si incrementarIntentosLogin lanza RuntimeException, propagarla
            System.err.println("❌ RuntimeException capturada en incrementarIntentosLoginPorEmail:");
            System.err.println("  - Email: " + email);
            System.err.println("  - Tipo: " + e.getClass().getName());
            System.err.println("  - Mensaje: " + e.getMessage());
            e.printStackTrace();
            // Re-lanzar para que el controlador la capture
            throw e;
        } catch (ExecutionException e) {
            System.err.println("❌ ExecutionException capturada en incrementarIntentosLoginPorEmail:");
            System.err.println("  - Email: " + email);
            System.err.println("  - Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (InterruptedException e) {
            System.err.println("❌ InterruptedException capturada en incrementarIntentosLoginPorEmail:");
            System.err.println("  - Email: " + email);
            System.err.println("  - Mensaje: " + e.getMessage());
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw e;
        }
    }
    
    // Resetear intentos de login (cuando el login es exitoso)
    // IMPORTANTE: Solo resetear intentos si la cuenta NO está bloqueada
    public void resetearIntentosLogin(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        DocumentSnapshot doc = docRef.get().get();
        
        if (!doc.exists()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }
        
        // Verificar si la cuenta está bloqueada
        Boolean isBlocked = doc.getBoolean("isBlocked");
        
        // LOG PARA DEBUG
        System.out.println("=== RESETEAR INTENTOS LOGIN ===");
        System.out.println("Usuario ID: " + userId);
        System.out.println("isBlocked: " + isBlocked);
        
        // IMPORTANTE: Si la cuenta está bloqueada, NO resetear intentos ni desbloquear
        // Solo el admin puede desbloquear cuentas
        if (isBlocked != null && isBlocked) {
            System.out.println("Usuario bloqueado - NO se resetean intentos. Solo admin puede desbloquear.");
            return;
        }
        
        // Solo resetear intentos si la cuenta NO está bloqueada
        Map<String, Object> updates = new HashMap<>();
        updates.put("loginAttempts", 0);
        docRef.update(updates).get();
        System.out.println("Intentos de login reseteados (usuario no bloqueado)");
        System.out.println("=== FIN RESETEAR INTENTOS ===");
    }
    
    // Desbloquear cuenta de usuario (solo admin)
    public UsuarioDTO desbloquearCuenta(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("isBlocked", false);
        updates.put("loginAttempts", 0);
        docRef.update(updates).get();
        return obtenerUsuarioPorId(userId);
    }
    
    // Verificar si la cuenta está bloqueada
    public boolean estaBloqueada(String userId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(userId).get().get();
        if (!doc.exists()) {
            return false;
        }
        Boolean isBlocked = doc.getBoolean("isBlocked");
        return isBlocked != null && isBlocked;
    }

    // Método auxiliar para convertir DocumentSnapshot a UsuarioDTO
    // CRÍTICO: Asegura que isBlocked siempre tenga un valor (false si no existe)
    private UsuarioDTO convertirAUsuarioDTO(DocumentSnapshot document) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(document.getId());
        dto.setEmail(document.getString("email"));
        dto.setName(document.getString("name"));
        dto.setAvatarUrl(document.getString("avatarUrl"));
        dto.setBio(document.getString("bio"));
        dto.setFollowers((List<String>) document.get("followers"));
        dto.setFollowing((List<String>) document.get("following"));
        dto.setInterests((List<String>) document.get("interests"));
        dto.setHobbies((List<String>) document.get("hobbies"));
        dto.setBioSoundUrl(document.getString("bioSoundUrl"));
        // Asegurar que onboardingComplete sea boolean (false si es null)
        Boolean onboardingComplete = document.getBoolean("onboardingComplete");
        dto.setOnboardingComplete(onboardingComplete != null ? onboardingComplete : false);
        dto.setRole(document.getString("role"));
        
        // CRÍTICO: Leer isBlocked DIRECTAMENTE desde Firestore SIN modificar
        // NO inicializar automáticamente para evitar sobrescribir valores bloqueados manualmente
        Boolean isBlocked = document.getBoolean("isBlocked");
        
        // LOG PARA DEBUG: Solo registrar si hay un problema con isBlocked
        if (isBlocked == null) {
            System.out.println("⚠ ADVERTENCIA: Campo isBlocked es null en Firestore para usuario: " + document.getId());
            System.out.println("  - Email: " + document.getString("email"));
            System.out.println("  - NOTA: isBlocked null significa que el campo no existe en Firestore");
            System.out.println("  - Si el usuario fue bloqueado manualmente, este campo DEBE ser true");
            System.out.println("  - Usando false como fallback, pero NO se guardará automáticamente");
            // NO guardar automáticamente para evitar sobrescribir valores bloqueados
            isBlocked = false; // Solo fallback en el DTO, NO guardar en Firestore
        } else {
            // Si isBlocked existe en Firestore, usar ese valor (puede ser true o false)
            System.out.println("✓ Campo isBlocked leído desde Firestore para usuario " + document.getId() + ": " + isBlocked);
        }
        
        dto.setIsBlocked(isBlocked);
        
        // CRÍTICO: Verificar que el valor leído sea correcto
        if (isBlocked) {
            System.out.println("⚠⚠⚠ USUARIO BLOQUEADO detectado al leer DTO:");
            System.out.println("  - Usuario ID: " + document.getId());
            System.out.println("  - Email: " + document.getString("email"));
            System.out.println("  - isBlocked en Firestore: " + isBlocked);
            System.out.println("  - isBlocked en DTO: " + dto.getIsBlocked());
        }
        
        // Incluir loginAttempts en el DTO
        Long loginAttemptsLong = document.getLong("loginAttempts");
        if (loginAttemptsLong == null) {
            // Si loginAttempts no existe, inicializarlo a 0 y guardarlo
            try {
                DocumentReference docRef = document.getReference();
                docRef.update("loginAttempts", 0).get();
                loginAttemptsLong = 0L;
                System.out.println("Campo loginAttempts inicializado para usuario: " + document.getId());
            } catch (Exception e) {
                System.err.println("Error al inicializar loginAttempts para usuario " + document.getId() + ": " + e.getMessage());
                loginAttemptsLong = 0L; // Fallback: asumir 0
            }
        }
        dto.setLoginAttempts(loginAttemptsLong.intValue());
        
        // LOG PARA DEBUG (solo para usuarios bloqueados o con intentos > 0)
        if (isBlocked || loginAttemptsLong > 0) {
            System.out.println("=== CONVERTIR A DTO (Usuario con bloqueo/intentos) ===");
            System.out.println("Usuario ID: " + document.getId());
            System.out.println("Email: " + document.getString("email"));
            System.out.println("isBlocked en Firestore: " + isBlocked);
            System.out.println("isBlocked en DTO: " + dto.getIsBlocked());
            System.out.println("loginAttempts en Firestore: " + loginAttemptsLong);
            System.out.println("loginAttempts en DTO: " + dto.getLoginAttempts());
            System.out.println("=== FIN CONVERTIR DTO ===");
        }
        
        return dto;
    }
}
