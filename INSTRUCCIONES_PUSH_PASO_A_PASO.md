# Cómo ejecutar git push origin main

## Paso 1: Abre la terminal en el directorio correcto

El comando `git push origin main` se ejecuta en la **terminal** (PowerShell, CMD, o Git Bash) desde el directorio del proyecto backend.

### Ubicación correcta:
```
C:\Users\Juan\OneDrive\Desktop\Proyecto\back_FriendlyVoice
```

---

## Paso 2: Ejecuta estos comandos en orden

### 1. Primero, haz commit de los cambios:

```bash
git add .
```

```bash
git commit -m "Improve Firebase credentials handling with automatic newline conversion"
```

### 2. Luego, intenta hacer push:

```bash
git push origin main
```

**NOTA:** Si aún está bloqueado, primero permite los secrets en GitHub usando los enlaces que te di antes.

---

## Paso 3: Si sigue bloqueado

Si el push sigue bloqueado, **primero permite los secrets** en GitHub:

1. Ve a: https://github.com/juandi251/FriendlyVoice-Back/security/secret-scanning/unblock-secret/34rhpWbvl9Wtk7ymVrYPWuCTUBH
2. Haz clic en "Allow secret"
3. Ve a: https://github.com/juandi251/FriendlyVoice-Back/security/secret-scanning/unblock-secret/34rhpTXe2gN433RAxRcfopg7gZw
4. Haz clic en "Allow secret" nuevamente
5. Luego ejecuta: `git push origin main`

---

## ¿Dónde ejecutar estos comandos?

- **PowerShell** (Windows): Abre PowerShell, navega al directorio con `cd C:\Users\Juan\OneDrive\Desktop\Proyecto\back_FriendlyVoice`
- **CMD** (Windows): Abre CMD, navega con `cd C:\Users\Juan\OneDrive\Desktop\Proyecto\back_FriendlyVoice`
- **Git Bash**: Abre Git Bash, navega con `cd /c/Users/Juan/OneDrive/Desktop/Proyecto/back_FriendlyVoice`
- **Terminal de VS Code/Cursor**: Abre la terminal integrada, asegúrate de estar en el directorio `back_FriendlyVoice`

---

## Verificación

Si quieres verificar en qué directorio estás:
```bash
pwd
```

Debería mostrar:
```
C:\Users\Juan\OneDrive\Desktop\Proyecto\back_FriendlyVoice
```

