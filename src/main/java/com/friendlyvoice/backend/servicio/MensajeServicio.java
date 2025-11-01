package com.friendlyvoice.backend.servicio;

import com.friendlyvoice.backend.modelo.Mensaje;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class MensajeServicio {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "messages";

    // Enviar mensaje directo
    public Mensaje enviarMensaje(String senderId, String recipientId, String voiceUrl) throws ExecutionException, InterruptedException {
        String chatId = generarChatId(senderId, recipientId);
        String messageId = "dm-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 9);

        Mensaje mensaje = new Mensaje();
        mensaje.setId(messageId);
        mensaje.setChatId(chatId);
        mensaje.setSenderId(senderId);
        mensaje.setRecipientId(recipientId);
        mensaje.setVoiceUrl(voiceUrl);
        mensaje.setCreatedAt(Instant.now().toString());
        mensaje.setIsRead(false);

        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(messageId);
        ApiFuture<WriteResult> future = docRef.set(mensaje);
        future.get();

        return mensaje;
    }

    // Obtener mensajes de un chat
    public List<Mensaje> obtenerMensajesDeChat(String userId, String chatPartnerId) throws ExecutionException, InterruptedException {
        String chatId = generarChatId(userId, chatPartnerId);

        try {
            // Intento 1: Intentar con orderBy si existe el índice
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("chatId", chatId)
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            return documents.stream()
                    .map(this::convertirAMensaje)
                    .collect(Collectors.toList());
        } catch (ExecutionException e) {
            // Firestore errors are wrapped in ExecutionException
            Throwable cause = e.getCause();
            String errorMessage = (cause != null ? cause.getMessage() : e.getMessage());
            
            // Si falla por falta de índice, obtener sin orderBy y ordenar en memoria
            if (errorMessage != null && (errorMessage.contains("index") || errorMessage.contains("FAILED_PRECONDITION"))) {
                // Fallback: obtener sin orderBy
                ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("chatId", chatId)
                        .get();

                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                // Ordenar en memoria por createdAt
                return documents.stream()
                        .map(this::convertirAMensaje)
                        .sorted((m1, m2) -> {
                            String date1 = m1.getCreatedAt() != null ? m1.getCreatedAt() : "";
                            String date2 = m2.getCreatedAt() != null ? m2.getCreatedAt() : "";
                            return date1.compareTo(date2);
                        })
                        .collect(Collectors.toList());
            } else {
                // Si es otro error, re-lanzar
                throw e;
            }
        } catch (Exception e) {
            // Capturar cualquier otra excepción
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("index") || errorMessage.contains("FAILED_PRECONDITION"))) {
                // Fallback: obtener sin orderBy
                try {
                    ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                            .whereEqualTo("chatId", chatId)
                            .get();

                    List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                    // Ordenar en memoria por createdAt
                    return documents.stream()
                            .map(this::convertirAMensaje)
                            .sorted((m1, m2) -> {
                                String date1 = m1.getCreatedAt() != null ? m1.getCreatedAt() : "";
                                String date2 = m2.getCreatedAt() != null ? m2.getCreatedAt() : "";
                                return date1.compareTo(date2);
                            })
                            .collect(Collectors.toList());
                } catch (Exception fallbackError) {
                    throw new RuntimeException("Error al obtener mensajes: " + fallbackError.getMessage(), fallbackError);
                }
            } else {
                throw new RuntimeException("Error al obtener mensajes: " + e.getMessage(), e);
            }
        }
    }

    // Marcar mensaje como leído
    public void marcarComoLeido(String messageId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(messageId);
        ApiFuture<WriteResult> future = docRef.update("isRead", true);
        future.get();
    }

    // Obtener mensajes no leídos para un usuario
    public List<Mensaje> obtenerMensajesNoLeidos(String userId) throws ExecutionException, InterruptedException {
        try {
            // Intento 1: Intentar con ambas condiciones si existe el índice
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("recipientId", userId)
                    .whereEqualTo("isRead", false)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            return documents.stream()
                    .map(this::convertirAMensaje)
                    .collect(Collectors.toList());
        } catch (ExecutionException e) {
            // Si falla por falta de índice, obtener todos los mensajes del usuario y filtrar en memoria
            Throwable cause = e.getCause();
            String errorMessage = (cause != null ? cause.getMessage() : e.getMessage());
            
            if (errorMessage != null && (errorMessage.contains("index") || errorMessage.contains("FAILED_PRECONDITION"))) {
                // Fallback: obtener solo por recipientId y filtrar en memoria
                ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("recipientId", userId)
                        .get();

                List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                // Filtrar mensajes no leídos en memoria
                return documents.stream()
                        .map(this::convertirAMensaje)
                        .filter(mensaje -> Boolean.FALSE.equals(mensaje.getIsRead()) || mensaje.getIsRead() == null)
                        .collect(Collectors.toList());
            } else {
                // Si es otro error, re-lanzar
                throw e;
            }
        } catch (Exception e) {
            // Capturar cualquier otra excepción
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("index") || errorMessage.contains("FAILED_PRECONDITION"))) {
                // Fallback: obtener solo por recipientId y filtrar en memoria
                try {
                    ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                            .whereEqualTo("recipientId", userId)
                            .get();

                    List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                    // Filtrar mensajes no leídos en memoria
                    return documents.stream()
                            .map(this::convertirAMensaje)
                            .filter(mensaje -> Boolean.FALSE.equals(mensaje.getIsRead()) || mensaje.getIsRead() == null)
                            .collect(Collectors.toList());
                } catch (Exception fallbackError) {
                    throw new RuntimeException("Error al obtener mensajes no leídos: " + fallbackError.getMessage(), fallbackError);
                }
            } else {
                throw new RuntimeException("Error al obtener mensajes no leídos: " + e.getMessage(), e);
            }
        }
    }

    // Obtener resumen de un chat (último mensaje y contador sin leer) - OPTIMIZADO
    public Map<String, Object> obtenerResumenChat(String userId, String chatPartnerId) throws ExecutionException, InterruptedException {
        String chatId = generarChatId(userId, chatPartnerId);
        Map<String, Object> resumen = new HashMap<>();
        Mensaje ultimoMensaje = null;
        int unreadCount = 0;
        
        try {
            // Obtener solo el último mensaje (limit 1, ordenado descendente por createdAt)
            ApiFuture<QuerySnapshot> futureUltimo = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("chatId", chatId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get();
            
            List<QueryDocumentSnapshot> docsUltimo = futureUltimo.get().getDocuments();
            if (!docsUltimo.isEmpty()) {
                ultimoMensaje = convertirAMensaje(docsUltimo.get(0));
            }
            
            // Obtener contador de mensajes sin leer (solo el count, no los mensajes completos)
            ApiFuture<QuerySnapshot> futureUnread = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("chatId", chatId)
                    .whereEqualTo("recipientId", userId)
                    .whereEqualTo("isRead", false)
                    .get();
            
            List<QueryDocumentSnapshot> docsUnread = futureUnread.get().getDocuments();
            unreadCount = docsUnread.size();
            
        } catch (ExecutionException e) {
            // Si falla por falta de índice en orderBy, intentar sin orderBy
            Throwable cause = e.getCause();
            String errorMessage = (cause != null ? cause.getMessage() : e.getMessage());
            
            if (errorMessage != null && (errorMessage.contains("index") || errorMessage.contains("FAILED_PRECONDITION"))) {
                // Fallback: obtener todos los mensajes del chat y calcular en memoria
                try {
                    ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                            .whereEqualTo("chatId", chatId)
                            .get();
                    
                    List<QueryDocumentSnapshot> documents = future.get().getDocuments();
                    
                    if (!documents.isEmpty()) {
                        // Ordenar en memoria y obtener el último
                        List<Mensaje> mensajes = documents.stream()
                                .map(this::convertirAMensaje)
                                .sorted((m1, m2) -> {
                                    String date1 = m1.getCreatedAt() != null ? m1.getCreatedAt() : "";
                                    String date2 = m2.getCreatedAt() != null ? m2.getCreatedAt() : "";
                                    return date2.compareTo(date1); // DESC para obtener el último
                                })
                                .collect(Collectors.toList());
                        
                        ultimoMensaje = mensajes.get(0);
                        
                        // Contar mensajes sin leer
                        unreadCount = (int) mensajes.stream()
                                .filter(m -> m.getRecipientId().equals(userId) && 
                                            Boolean.FALSE.equals(m.getIsRead()))
                                .count();
                    } else {
                        ultimoMensaje = null;
                        unreadCount = 0;
                    }
                } catch (Exception fallbackError) {
                    ultimoMensaje = null;
                    unreadCount = 0;
                }
            } else {
                // Si es otro error, mantener valores por defecto
                ultimoMensaje = null;
                unreadCount = 0;
            }
        } catch (Exception e) {
            // Cualquier otra excepción, mantener valores por defecto
            ultimoMensaje = null;
            unreadCount = 0;
        }
        
        resumen.put("lastMessage", ultimoMensaje);
        resumen.put("unreadCount", unreadCount);
        return resumen;
    }

    // Generar ID de chat consistente (ordenado alfabéticamente)
    private String generarChatId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return String.join("_", ids);
    }

    // Método auxiliar para convertir DocumentSnapshot a Mensaje
    private Mensaje convertirAMensaje(DocumentSnapshot document) {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(document.getId());
        mensaje.setChatId(document.getString("chatId"));
        mensaje.setSenderId(document.getString("senderId"));
        mensaje.setRecipientId(document.getString("recipientId"));
        mensaje.setVoiceUrl(document.getString("voiceUrl"));
        mensaje.setCreatedAt(document.getString("createdAt"));
        mensaje.setIsRead(document.getBoolean("isRead"));
        return mensaje;
    }
}
