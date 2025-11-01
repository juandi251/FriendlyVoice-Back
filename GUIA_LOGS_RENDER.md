# Guía para Validar Logs de Firebase en Render

## Cómo acceder a los logs en Render:

1. Ve a tu servicio en Render (https://dashboard.render.com)
2. Haz clic en tu servicio backend
3. Ve a la pestaña **"Logs"** (está en la parte superior)
4. Los logs se actualizan en tiempo real

## Qué buscar en los logs:

### 1. Busca estas líneas específicas (en orden):

```
=== Inicializando Firebase Admin SDK ===
```

**Si ves esta línea:** ✅ La aplicación está intentando inicializar Firebase

---

```
Verificando variable de entorno FIREBASE_SERVICE_ACCOUNT...
```

**Si ves esta línea:** ✅ El código está verificando la variable

---

```
Variable encontrada: SÍ
```

**✅ BUENO:** La variable está configurada

**❌ MALO:** Si dice `Variable encontrada: NO`, entonces:
- La variable `FIREBASE_SERVICE_ACCOUNT` NO está configurada en Render
- **Solución:** Ve a Render → Environment → Agrega la variable

---

### 2. Si la variable existe, busca:

```
Tamaño del JSON: X caracteres
```

**✅ BUENO:** Si el tamaño es mayor a 500 caracteres (un JSON normal tiene entre 1000-3000 caracteres)

**❌ MALO:** Si el tamaño es 0:
- La variable está vacía
- **Solución:** Asegúrate de pegar el JSON completo en Render

---

```
Primeros 100 caracteres: {"type":"service_account","project_id":...
```

**✅ BUENO:** Si empieza con `{"type":"service_account"` → El JSON está bien formateado

**❌ MALO:** Si empieza con algo diferente o tiene caracteres raros:
- El JSON está mal formateado o tiene caracteres extra
- **Solución:** Verifica que el JSON esté en una sola línea, sin saltos de línea

---

### 3. Si todo va bien, deberías ver:

```
✓ Stream creado desde variable de entorno
Parseando credenciales desde stream...
✓ Credenciales de Google parseadas correctamente
✓ Firebase Admin SDK inicializado correctamente
=== Firebase Configuración Completada ===
```

**✅ Si ves todos estos checkmarks (✓):** Firebase se inicializó correctamente

---

### 4. Si hay un error, busca:

```
ERROR CRÍTICO al inicializar Firebase
==========================================
Tipo de error: ...
Mensaje: ...
```

**Esto te dirá exactamente qué está mal:**

- Si el **Tipo de error** es `IOException`:
  - Probablemente el JSON está mal formateado
  - **Solución:** Valida el JSON en https://jsonlint.com
  
- Si el **Mensaje** dice algo sobre "parse" o "JSON":
  - El JSON tiene caracteres inválidos
  - **Solución:** Asegúrate de que el JSON esté en una sola línea sin saltos de línea

---

### 5. Si no encuentra la variable, verás:

```
Variable encontrada: NO
Variable de entorno FIREBASE_SERVICE_ACCOUNT no encontrada o vacía, intentando otras fuentes...
Intentando cargar desde classpath: /firebase-service-account.local.json
ERROR: Firebase service account file NOT FOUND en classpath: /firebase-service-account.local.json
```

**Esto significa:**
- La variable NO está configurada en Render
- Intentó buscar el archivo local (que no existe en producción)
- **Solución:** Configura `FIREBASE_SERVICE_ACCOUNT` en Render → Environment

---

## Resumen de líneas clave:

| Línea en logs | Estado | Acción |
|---------------|--------|--------|
| `Variable encontrada: NO` | ❌ Error | Configurar variable en Render |
| `Variable encontrada: SÍ` | ✅ OK | Continuar revisando |
| `Tamaño del JSON: 0` | ❌ Error | Variable vacía, pegar JSON completo |
| `Tamaño del JSON: XXXX` (X > 500) | ✅ OK | Variable tiene contenido |
| `ERROR CRÍTICO al inicializar Firebase` | ❌ Error | Revisar tipo y mensaje de error |
| `✓ Firebase Admin SDK inicializado correctamente` | ✅ OK | ¡Todo funciona! |

---

## Cómo configurar la variable correctamente:

1. **Obtén el JSON:**
   - Abre tu archivo `firebase-service-account.json` (local)
   - Copia TODO el contenido

2. **Formatea el JSON:**
   - Debe estar en UNA sola línea (sin saltos de línea)
   - Puedes usar: https://jsonformatter.org/json-minify (opción "Minify")

3. **Pega en Render:**
   - Ve a Render → Tu servicio → Environment
   - Click en "Add Environment Variable"
   - Key: `FIREBASE_SERVICE_ACCOUNT`
   - Value: Pega el JSON completo (una línea)
   - Guarda

4. **Redeploy:**
   - Render debería detectar el cambio automáticamente
   - O haz "Manual Deploy"

5. **Verifica los logs:**
   - Espera a que termine el build
   - Revisa los logs buscando las líneas de arriba

---

## Endpoints de diagnóstico (cuando la app esté corriendo):

Una vez que la aplicación arranque (aunque tenga errores de Firebase), puedes usar:

- **Health check:** `GET https://tu-url.onrender.com/api/diagnostico/health`
  - Debe responder con: `{"status":"UP","timestamp":...}`
  
- **Diagnóstico Firebase:** `GET https://tu-url.onrender.com/api/diagnostico/firebase`
  - Te muestra el estado de la variable y Firebase

---

## Si sigues teniendo problemas:

1. Copia los logs completos desde "=== Inicializando Firebase Admin SDK ===" hasta el error final
2. Compártelos para revisarlos juntos
3. Especialmente busca las líneas que dicen:
   - `Variable encontrada:`
   - `Tamaño del JSON:`
   - `ERROR CRÍTICO`
   - `Tipo de error:`
   - `Mensaje:`

