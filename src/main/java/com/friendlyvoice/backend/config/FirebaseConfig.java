package com.friendlyvoice.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.config.path:classpath:/firebase-service-account.local.json}")
    private String firebaseConfigPath;

    @Value("${firebase.database.url:https://friendlyvoice-app-default-rtdb.firebaseio.com}")
    private String databaseUrl;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("=== Inicializando Firebase Admin SDK ===");
            System.out.println("URL de base de datos: " + databaseUrl);
            System.out.println("Ruta de configuración: " + firebaseConfigPath);
            
            InputStream serviceAccount = null;
            
            // PRIORIDAD 1: Intentar cargar desde variable de entorno (producción - Render)
            String firebaseServiceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            System.out.println("Verificando variable de entorno FIREBASE_SERVICE_ACCOUNT...");
            System.out.println("Variable encontrada: " + (firebaseServiceAccountJson != null ? "SÍ" : "NO"));
            
            if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.isEmpty()) {
                System.out.println("Intentando cargar credenciales desde variable de entorno FIREBASE_SERVICE_ACCOUNT");
                System.out.println("Tamaño del JSON: " + firebaseServiceAccountJson.length() + " caracteres");
                System.out.println("Primeros 100 caracteres: " + firebaseServiceAccountJson.substring(0, Math.min(100, firebaseServiceAccountJson.length())));
                try {
                    String jsonToUse = firebaseServiceAccountJson.trim();
                    
                    // Verificar que empieza y termina correctamente
                    if (!jsonToUse.startsWith("{")) {
                        throw new IOException("El JSON no empieza con '{'. Verifique el formato.");
                    }
                    if (!jsonToUse.endsWith("}")) {
                        throw new IOException("El JSON no termina con '}'. Verifique el formato.");
                    }
                    
                    // Verificar que el JSON pueda parsearse correctamente
                    // Solo hacer correcciones si es absolutamente necesario
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        // Intentar parsear el JSON directamente (debería funcionar si está bien formateado)
                        JsonNode jsonNode = mapper.readTree(jsonToUse);
                        // Si se puede parsear, usar el JSON tal cual (está correcto)
                        System.out.println("✓ JSON válido y parseable, tamaño: " + jsonToUse.length() + " caracteres");
                        // No reconstruir, usar el original que funciona
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        System.err.println("ADVERTENCIA: JSON no puede parsearse, intentando normalizar...");
                        System.err.println("Error: " + e.getMessage());
                        
                        // Solo si falla el parse, intentar normalizar
                        // Eliminar saltos de línea reales entre propiedades (fuera de strings)
                        jsonToUse = jsonToUse
                            .replace("\r\n", " ")
                            .replace("\r", " ")
                            .replace("\n", " ")
                            .replaceAll(" +", " ")
                            .trim();
                        
                        // Intentar parsear de nuevo
                        try {
                            JsonNode jsonNode = mapper.readTree(jsonToUse);
                            jsonToUse = mapper.writeValueAsString(jsonNode);
                            System.out.println("✓ JSON normalizado y reconstruido correctamente");
                        } catch (com.fasterxml.jackson.core.JsonProcessingException e2) {
                            System.err.println("ERROR: JSON aún no puede parsearse después de normalización");
                            throw new IOException("El JSON no puede parsearse: " + e2.getMessage(), e2);
                        }
                    }
                    
                    serviceAccount = new ByteArrayInputStream(jsonToUse.getBytes("UTF-8"));
                    System.out.println("✓ Stream creado desde variable de entorno");
                } catch (Exception e) {
                    System.err.println("ERROR al crear stream desde variable de entorno: " + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("Failed to create stream from FIREBASE_SERVICE_ACCOUNT environment variable: " + e.getMessage(), e);
                }
            } else {
                System.out.println("Variable de entorno FIREBASE_SERVICE_ACCOUNT no encontrada o vacía, intentando otras fuentes...");
            }
            // PRIORIDAD 2: Intentar cargar desde classpath (desarrollo local)
            if (serviceAccount == null && firebaseConfigPath != null && firebaseConfigPath.startsWith("classpath:")) {
                String path = firebaseConfigPath.replace("classpath:", "");
                System.out.println("Intentando cargar desde classpath: " + path);
                serviceAccount = getClass().getResourceAsStream(path);
                if (serviceAccount != null) {
                    System.out.println("✓ Archivo de credenciales cargado desde classpath");
                } else {
                    System.err.println("ERROR: Firebase service account file NOT FOUND en classpath: " + path);
                    System.err.println("Asegúrate de que el archivo firebase-service-account.local.json existe en src/main/resources/");
                    System.err.println("NOTA: En producción (Render), debe configurar la variable de entorno FIREBASE_SERVICE_ACCOUNT");
                    throw new IOException("Firebase service account file not found in classpath: " + path + 
                        ". Configure FIREBASE_SERVICE_ACCOUNT environment variable for production.");
                }
            }
            // PRIORIDAD 3: Intentar cargar desde sistema de archivos
            if (serviceAccount == null && firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
                System.out.println("Intentando cargar desde sistema de archivos: " + firebaseConfigPath);
                try {
                    serviceAccount = new FileInputStream(firebaseConfigPath);
                    System.out.println("✓ Archivo de credenciales cargado desde sistema de archivos");
                } catch (IOException e) {
                    System.err.println("ERROR: No se pudo cargar el archivo de credenciales desde: " + firebaseConfigPath);
                    throw e;
                }
            }
            
            if (serviceAccount == null) {
                System.err.println("ERROR: No se pudo cargar credenciales de Firebase desde ninguna fuente.");
                System.err.println("Fuentes intentadas:");
                System.err.println("  1. Variable de entorno FIREBASE_SERVICE_ACCOUNT: " + (System.getenv("FIREBASE_SERVICE_ACCOUNT") != null ? "EXISTE pero está vacía" : "NO EXISTE"));
                System.err.println("  2. Classpath: " + firebaseConfigPath);
                throw new IOException("No se encontraron credenciales de Firebase. " +
                    "Para producción (Render), configure la variable de entorno FIREBASE_SERVICE_ACCOUNT con el JSON completo.");
            }

            try {
                System.out.println("Parseando credenciales desde stream...");
                
                // Crear credenciales con refresh de tokens
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                // IMPORTANTE: Crear credenciales con acceso a Firestore
                credentials = credentials.createScoped("https://www.googleapis.com/auth/cloud-platform");
                
                System.out.println("✓ Credenciales de Google parseadas correctamente");
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✓ Firebase Admin SDK inicializado correctamente");
                System.out.println("=== Firebase Configuración Completada ===");
            } catch (Exception e) {
                System.err.println("==========================================");
                System.err.println("ERROR CRÍTICO al inicializar Firebase");
                System.err.println("==========================================");
                System.err.println("Tipo de error: " + e.getClass().getName());
                System.err.println("Mensaje: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Causa: " + e.getCause().getClass().getName());
                    System.err.println("Mensaje de causa: " + e.getCause().getMessage());
                }
                System.err.println("------------------------------------------");
                System.err.println("POSIBLES SOLUCIONES:");
                System.err.println("1. Verifique que FIREBASE_SERVICE_ACCOUNT contenga un JSON válido");
                System.err.println("2. El JSON debe estar en una sola línea sin saltos de línea");
                System.err.println("3. Verifique que todas las comillas estén correctamente escapadas");
                System.err.println("4. Pruebe validar el JSON en un validador online");
                System.err.println("==========================================");
                e.printStackTrace();
                throw new IOException("Failed to initialize Firebase: " + e.getMessage(), e);
            } finally {
                if (serviceAccount != null) {
                    try {
                        serviceAccount.close();
                    } catch (IOException e) {
                        System.err.println("Error al cerrar stream: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("Firebase Admin SDK ya está inicializado");
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
