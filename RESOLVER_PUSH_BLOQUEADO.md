# Cómo resolver el push bloqueado por GitHub

GitHub está bloqueando el push porque detectó credenciales de Firebase en commits anteriores.

## ✅ Opción 1: Permitir el secret (RÁPIDO - Recomendado)

Esta opción es la más rápida y sencilla. GitHub te permite autorizar estos secrets específicos.

### Pasos:

1. **Primer secret - Abre este enlace:**
   ```
   https://github.com/juandi251/FriendlyVoice-Back/security/secret-scanning/unblock-secret/34rhpWbvl9Wtk7ymVrYPWuCTUBH
   ```

2. **Haz clic en "Allow secret"** (o "Permitir secret" en español)

3. **Segundo secret - Abre este enlace:**
   ```
   https://github.com/juandi251/FriendlyVoice-Back/security/secret-scanning/unblock-secret/34rhpTXe2gN433RAxRcfopg7gZw
   ```

4. **Haz clic en "Allow secret"** nuevamente

5. **Ejecuta el push:**
   ```bash
   git push origin main
   ```

**Ventajas:** ✅ Rápido ✅ No necesitas cambiar el historial ✅ Los cambios se despliegan inmediatamente

---

## 🔧 Opción 2: Eliminar los commits con credenciales (ALTERNATIVA)

Si prefieres no permitir los secrets, puedes reescribir el historial para eliminar esos commits.

### Pasos:

1. **Ver los últimos commits:**
   ```bash
   git log --oneline -5
   ```

2. **Identificar el commit problemático:** `53cc382` (Add root endpoint...)

3. **Reescribir el historial para eliminar ese commit:**
   ```bash
   git rebase -i 53cc382^
   ```

4. **En el editor que se abre:**
   - Cambia `pick` por `drop` para el commit `53cc382`
   - O simplemente elimina la línea de ese commit
   - Guarda y cierra

5. **Force push (¡CUIDADO!):**
   ```bash
   git push --force origin main
   ```

**⚠️ ADVERTENCIA:** Esto reescribe el historial. Solo hazlo si estás seguro.

---

## 💡 Recomendación

**Usa la Opción 1** - Es más rápida, más segura y GitHub te permite autorizar estos secrets específicos. Los archivos con credenciales ya fueron eliminados del código actual, solo están en el historial de commits anteriores.

---

## 📝 Nota importante

Los archivos con credenciales (`validacion-json.txt` y `firebase-service-account-formato-correcto.txt`) ya fueron eliminados del código. Solo existen en el historial de commits antiguos. Una vez que permitas los secrets o reescribas el historial, podrás hacer push normalmente.

