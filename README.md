# Backend FriendlyVoice

Backend API REST para FriendlyVoice construido con Spring Boot 3.4.8 y Firebase Firestore.

## 🚀 Tecnologías

- **Java 17**
- **Spring Boot 3.4.8**
- **Firebase Admin SDK 9.3.0**
- **Maven**
- **Lombok**

## 📋 Requisitos Previos

1. **Java 17** instalado
2. **Maven** instalado
3. **IntelliJ IDEA** (recomendado)
4. **Cuenta de Firebase** con proyecto configurado

## 🔧 Configuración

### 1. Obtener credenciales de Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `friendlyvoice-app`
3. Ve a **Configuración del proyecto** (ícono de engranaje) → **Cuentas de servicio**
4. Haz clic en **Generar nueva clave privada**
5. Descarga el archivo JSON

### 2. Configurar el proyecto

1. Coloca el archivo JSON descargado en `src/main/resources/` con el nombre `firebase-service-account.json`

2. Verifica que el archivo esté en `.gitignore` (ya está configurado)

3. Ajusta `src/main/resources/application.properties` si es necesario:
   ```properties
   server.port=8080
   firebase.config.path=classpath:firebase-service-account.json
   firebase.database.url=https://friendlyvoice-app-default-rtdb.firebaseio.com
   cors.allowed.origins=http://localhost:9002,http://localhost:3000
   ```

## 🏃 Ejecutar el Proyecto

### Desde IntelliJ IDEA

1. Abre el proyecto en IntelliJ IDEA
2. Espera a que Maven descargue las dependencias
3. Busca la clase `BackFriendlyVoiceApplication.java`
4. Haz clic derecho → **Run 'BackFriendlyVoiceApplication'**

### Desde línea de comandos

```bash
# Compilar
mvn clean install

# Ejecutar
mvn spring-boot:run
```

El servidor estará disponible en `http://localhost:8080`

## 📡 Endpoints API

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/usuarios` | Obtener todos los usuarios |
| GET | `/api/usuarios/{userId}` | Obtener usuario por ID |
| GET | `/api/usuarios/buscar/{nombre}` | Buscar usuarios por nombre |
| PUT | `/api/usuarios/{userId}` | Actualizar perfil de usuario |
| POST | `/api/usuarios/{userId}/seguir/{userIdToFollow}` | Seguir a un usuario |
| DELETE | `/api/usuarios/{userId}/seguir/{userIdToUnfollow}` | Dejar de seguir |
| GET | `/api/usuarios/{userId}/mutuos` | Obtener seguidores mutuos |
| POST | `/api/usuarios/{userId}/onboarding` | Completar onboarding |

### Mensajes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/mensajes/enviar/{senderId}` | Enviar mensaje directo |
| GET | `/api/mensajes/chat/{userId}/{chatPartnerId}` | Obtener mensajes de un chat |
| PUT | `/api/mensajes/{messageId}/leido` | Marcar mensaje como leído |
| GET | `/api/mensajes/no-leidos/{userId}` | Obtener mensajes no leídos |

## 📦 Estructura del Proyecto

```
back_FriendlyVoice/
├── src/
│   ├── main/
│   │   ├── java/com/friendlyvoice/backend/
│   │   │   ├── config/
│   │   │   │   ├── FirebaseConfig.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── controlador/
│   │   │   │   ├── UsuarioControlador.java
│   │   │   │   └── MensajeControlador.java
│   │   │   ├── servicio/
│   │   │   │   ├── UsuarioServicio.java
│   │   │   │   └── MensajeServicio.java
│   │   │   ├── modelo/
│   │   │   │   ├── Usuario.java
│   │   │   │   └── Mensaje.java
│   │   │   ├── dto/
│   │   │   │   ├── UsuarioDTO.java
│   │   │   │   ├── ActualizarPerfilDTO.java
│   │   │   │   └── MensajeDTO.java
│   │   │   └── BackFriendlyVoiceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── firebase-service-account.json (NO COMMITEAR)
│   └── test/
├── pom.xml
└── README.md
```

## 🔒 Seguridad

- Las credenciales de Firebase están en `.gitignore`
- CORS configurado para permitir solo orígenes específicos
- Las reglas de seguridad de Firestore deben configurarse en Firebase Console

## 🧪 Probar la API

### Ejemplo con cURL

```bash
# Obtener todos los usuarios
curl http://localhost:8080/api/usuarios

# Obtener usuario por ID
curl http://localhost:8080/api/usuarios/USER_ID

# Actualizar perfil
curl -X PUT http://localhost:8080/api/usuarios/USER_ID \
  -H "Content-Type: application/json" \
  -d '{"name":"Nuevo Nombre","bio":"Mi biografía"}'

# Seguir usuario
curl -X POST http://localhost:8080/api/usuarios/USER_ID/seguir/TARGET_USER_ID

# Enviar mensaje
curl -X POST http://localhost:8080/api/mensajes/enviar/SENDER_ID \
  -H "Content-Type: application/json" \
  -d '{"recipientId":"RECIPIENT_ID","voiceUrl":"https://example.com/audio.mp3"}'
```

## 🐛 Troubleshooting

### Error: "Firebase service account file not found"
- Verifica que `firebase-service-account.json` esté en `src/main/resources/`
- Verifica que el nombre del archivo sea exacto

### Error: "Port 8080 already in use"
- Cambia el puerto en `application.properties`: `server.port=8081`

### Error de permisos en Firestore
- Verifica las reglas de seguridad en Firebase Console
- Para desarrollo, puedes usar reglas permisivas (NO para producción):
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /{document=**} {
        allow read, write: if true;
      }
    }
  }
  ```

## 📝 Notas

- Este backend está diseñado para trabajar con el frontend Next.js de FriendlyVoice
- La autenticación se maneja en el frontend con Firebase Auth
- Este backend proporciona operaciones CRUD sobre Firestore
- Para producción, considera agregar autenticación JWT y validación de tokens

## 🤝 Integración con Frontend

El frontend en `FriendlyVoice-App` puede consumir esta API cambiando las llamadas directas a Firebase por llamadas HTTP a estos endpoints.

Ejemplo en el frontend:
```typescript
// En lugar de usar Firebase directamente
const response = await fetch('http://localhost:8080/api/usuarios/USER_ID');
const usuario = await response.json();
```

## 📄 Licencia

Este proyecto es parte de FriendlyVoice.
