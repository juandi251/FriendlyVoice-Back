package com.friendlyvoice.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            
            InputStream serviceAccount = null;
            
            // PRIORIDAD 1: Intentar cargar desde variable de entorno (producción - Render)
            String firebaseServiceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT");
            if (firebaseServiceAccountJson != null && !firebaseServiceAccountJson.isEmpty()) {
                System.out.println("Intentando cargar credenciales desde variable de entorno FIREBASE_SERVICE_ACCOUNT");
                System.out.println("Tamaño del JSON: " + firebaseServiceAccountJson.length() + " caracteres");
                try {
                    serviceAccount = new ByteArrayInputStream(firebaseServiceAccountJson.getBytes("UTF-8"));
                    System.out.println("✓ Credenciales cargadas desde variable de entorno");
                } catch (Exception e) {
                    System.err.println("ERROR al parsear JSON desde variable de entorno: " + e.getMessage());
                    e.printStackTrace();
                    throw new IOException("Failed to parse Firebase credentials from environment variable", e);
                }
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
                    throw new IOException("Firebase service account file not found in classpath: " + path);
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
                throw new IOException("No se encontraron credenciales de Firebase. Configure FIREBASE_SERVICE_ACCOUNT o firebase.config.path");
            }

            try {
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                System.out.println("✓ Credenciales de Google parseadas correctamente");
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setDatabaseUrl(databaseUrl)
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("✓ Firebase Admin SDK inicializado correctamente");
                System.out.println("=== Firebase Configuración Completada ===");
            } catch (IOException e) {
                System.err.println("ERROR al inicializar Firebase:");
                System.err.println("  - Mensaje: " + e.getMessage());
                System.err.println("  - Causa: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
                System.err.println("Verifica que las credenciales sean válidas y tengan el formato correcto.");
                throw e;
            } finally {
                if (serviceAccount != null) {
                    serviceAccount.close();
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
