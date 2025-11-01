package com.friendlyvoice.backend.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.friendlyvoice.backend.servicio.ReporteServicio;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    @Autowired
    private ReporteServicio reporteServicio;

    // Crear reporte de publicación
    @PostMapping
    public ResponseEntity<?> crearReporte(@RequestBody Map<String, Object> data) {
        try {
            String vozId = (String) data.get("vozId");
            String userId = (String) data.get("userId");
            String motivo = (String) data.getOrDefault("motivo", "Contenido inapropiado");
            String mensaje = (String) data.getOrDefault("mensaje", null);
            
            Map<String, Object> reporte = reporteServicio.crearReporte(vozId, userId, motivo, mensaje);
            return ResponseEntity.status(HttpStatus.CREATED).body(reporte);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener todos los reportes
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosReportes() {
        try {
            List<Map<String, Object>> reportes = reporteServicio.obtenerTodosLosReportes();
            return ResponseEntity.ok(reportes);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar reporte (después de revisión)
    @DeleteMapping("/{reporteId}")
    public ResponseEntity<?> eliminarReporte(@PathVariable String reporteId) {
        try {
            reporteServicio.eliminarReporte(reporteId);
            return ResponseEntity.ok(Map.of("message", "Reporte eliminado"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

