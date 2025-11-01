package com.friendlyvoice.backend.servicio;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ReporteServicio {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "reports";

    // Crear reporte
    public Map<String, Object> crearReporte(String vozId, String userId, String motivo, String mensaje) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("vozId", vozId);
        reporte.put("userId", userId);
        reporte.put("motivo", motivo);
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            reporte.put("mensaje", mensaje.trim());
        }
        reporte.put("createdAt", new Date());
        reporte.put("status", "pendiente");
        
        ApiFuture<WriteResult> future = docRef.set(reporte);
        future.get();
        
        reporte.put("id", docRef.getId());
        return reporte;
    }

    // Obtener todos los reportes
    public List<Map<String, Object>> obtenerTodosLosReportes() throws ExecutionException, InterruptedException {
        try {
            // Intentar ordenar por createdAt si hay índice
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get();
            
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Map<String, Object>> reportes = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                Map<String, Object> reporte = doc.getData();
                reporte.put("id", doc.getId());
                reportes.add(reporte);
            }
            
            // Ordenar manualmente por fecha (fallback si orderBy falla)
            reportes.sort((a, b) -> {
                Object dateA = a.get("createdAt");
                Object dateB = b.get("createdAt");
                if (dateA == null || dateB == null) return 0;
                if (dateA instanceof Date && dateB instanceof Date) {
                    return ((Date) dateB).compareTo((Date) dateA); // DESC
                }
                return 0;
            });
            
            return reportes;
        } catch (Exception e) {
            // Si falla el orderBy (por ejemplo, falta de índice), obtener sin ordenar
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Map<String, Object>> reportes = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                Map<String, Object> reporte = doc.getData();
                reporte.put("id", doc.getId());
                reportes.add(reporte);
            }
            
            // Ordenar manualmente por fecha
            reportes.sort((a, b) -> {
                Object dateA = a.get("createdAt");
                Object dateB = b.get("createdAt");
                if (dateA == null || dateB == null) return 0;
                if (dateA instanceof Date && dateB instanceof Date) {
                    return ((Date) dateB).compareTo((Date) dateA); // DESC
                }
                return 0;
            });
            
            return reportes;
        }
    }

    // Eliminar reporte
    public void eliminarReporte(String reporteId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(reporteId);
        ApiFuture<WriteResult> future = docRef.delete();
        future.get();
    }
}

