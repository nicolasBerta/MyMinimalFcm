# MyMinimalFcm – Demo APK para pruebas de Firebase Cloud Messaging (FCM)

Pequeña app Android pensada para **probar notificaciones push** end-to-end con **Firebase** y un **backend (Spring Boot)**.  
Incluye **dos flavors** para simular 2 entidades/clientes distintos, cada uno con su propio `google-services.json`.

---

## 🎯 Objetivo

- Obtener el **token FCM** del dispositivo y copiarlo fácilmente.
- **Recibir notificaciones** (tanto `notification` como `data`) y mostrarlas con un canal de notificación básico.
- Probar **múltiples credenciales** de Firebase (una por entidad) usando **product flavors**.

---

## 📁 Estructura del proyecto

```
MyMinimalFcm/
├─ settings.gradle.kts
├─ build.gradle.kts
├─ gradle/wrapper/gradle-wrapper.properties
└─ app/
   ├─ build.gradle.kts
   ├─ proguard-rules.pro
   └─ src/
      ├─ main/
      │  ├─ AndroidManifest.xml
      │  ├─ java/com/nico/testpush/MainActivity.kt
      │  ├─ java/com/nico/testpush/MyFcmService.kt
      │  └─ res/layout/activity_main.xml
      ├─ fiid0001/
      │  └─ google-services.json   ← credencial Firebase del proyecto 1
      └─ fiid0002/
         └─ google-services.json   ← credencial Firebase del proyecto 2
```

> **Importante:** cada flavor tiene **su** `google-services.json`. No pongas un JSON en `src/main`.

---

## 🧩 Funcionalidad

- **MainActivity**
  - Botón **“OBTENER TOKEN”** → solicita permiso de notificaciones (Android 13+) y obtiene el token FCM.
  - Botón **“COPIAR”** → copia el token al portapapeles.
  - Muestra el token y logs (Logcat tag `FCM`).

- **MyFcmService**
  - `onNewToken(token)` → punto ideal para reportarlo a tu backend.
  - `onMessageReceived(message)` → arma y muestra una notificación:
    - Usa `notification.title/body` o `data.title/body`.
    - Crea el canal `push_basic` (Android 8+).

---

## 🧪 Flavors / Entidades

Definidos en `app/build.gradle.kts`:

- **fiid0001** → `applicationIdSuffix = ".fiid0001"`
- **fiid0002** → `applicationIdSuffix = ".fiid0002"`

Resultado: dos APKs con packages distintos (`com.nico.testpush.fiid0001` y `com.nico.testpush.fiid0002`).  
En **Firebase** cada app Android debe registrarse con **ese** package y su `google-services.json` correspondiente.

---

## 🔧 Requisitos

- Android Studio **Narwhal** o superior.
- Gradle wrapper **8.10.x**.
- Android Gradle Plugin **8.7.x**.
- Kotlin **2.0.x**.
- **JDK 21** (recomendado; el proyecto está preparado)  
  > Si usás Java 21 con `minSdk 24`, el proyecto habilita **desugaring**.

---

## 🚀 Configuración de Firebase (por flavor)

1. En **Firebase Console** → Proyecto X → *Add app* → **Android**.
2. **Package name**:
   - `fiid0001` → `com.nico.testpush.fiid0001`
   - `fiid0002` → `com.nico.testpush.fiid0002`
3. Descargar **`google-services.json`** y guardarlo en:
   - `app/src/fiid0001/google-services.json`
   - `app/src/fiid0002/google-services.json`

> Verificá que el JSON tenga `"client" → "android_client_info" → "package_name"` coincidente con el package del flavor.

---

## 🧱 Build & APKs

Desde Android Studio (Build Variants) o CLI:

```bash
# limpiar
./gradlew clean

# compilar fiid0001 y fiid0002 (debug)
./gradlew :app:assembleFiid0001Debug
./gradlew :app:assembleFiid0002Debug
```

APKs generadas:

```
app/build/outputs/apk/fiid0001/debug/app-fiid0001-debug.apk
app/build/outputs/apk/fiid0002/debug/app-fiid0002-debug.apk
```

---

## 📲 Uso

1. Instalar la APK del flavor deseado en el dispositivo/emulador.
2. Abrir la app → **OBTENER TOKEN** → **COPIAR**.
3. Registrar ese **token** en tu backend junto al **fiid** y el **cuil** (tu flujo).
4. Enviar una notificación desde el backend a ese token.

---

## 📤 Ejemplos de envío desde backend

### Java (Spring / Admin SDK)

```java
Message message = Message.builder()
    .setToken(token) // token devuelto por la app
    .setNotification(new Notification("Test push", "Hola desde backend"))
    .putData("extra", "123")
    .build();

String id = FirebaseMessaging.getInstance(firebaseAppDeLaEntidad).send(message);
```

> Donde `firebaseAppDeLaEntidad` se crea con el `ServiceAccount` (JSON) **de esa entidad**.  
> Podés cachear `FirebaseApp` por `fiid` y construirlo dinámicamente desde el JSON en BD.

### HTTP v1 con `curl` (rápido para pruebas)

```bash
# Con gcloud autenticado al proyecto de Firebase:
ACCESS_TOKEN="$(gcloud auth print-access-token)"

curl -X POST   -H "Authorization: Bearer ${ACCESS_TOKEN}"   -H "Content-Type: application/json; charset=UTF-8"   https://fcm.googleapis.com/v1/projects/<PROJECT_ID>/messages:send   -d '{
    "message": {
      "token": "<TOKEN_FCM>",
      "notification": { "title": "Test push", "body": "Hola!" },
      "data": { "extra": "123" }
    }
  }'
```

---

## 🔔 Tipos de mensajes

- **`notification`**: Android los muestra automáticamente (background).  
- **`data`**: siempre llegan a `onMessageReceived`; armás la notificación vos (útil para lógica propia).  
- Podés enviar **mixtos**: `notification` + `data`.

---

## 🔒 Buenas prácticas

- **Canales**: usá un `NotificationChannel` estable. No lo cambies entre versiones.
- **Token rotation**: escuchá `onNewToken` y actualizá tu backend si cambia.
- **Permisos**: Android 13+ requiere `POST_NOTIFICATIONS`.
- **Batería**: en algunos OEMs, desactivar optimizaciones si el push en background es crítico.

---

## 🛠️ Troubleshooting

- **`No matching client found for package name …`**  
  El `package_name` del `google-services.json` no coincide con el package del flavor (o JSON en carpeta incorrecta).

- **`Missing project_info object`**  
  Bajaste el JSON de **Web**. Necesitás el de **Android**.

- **Error de JDK / toolchain**  
  Alineá Java/Kotlin (1.8 / 17 / 21) y, si usás 21, dejá **Gradle JDK = Embedded JDK (21)**.  
  Con `minSdk < 26`, mantené **desugaring** activo.

- **Errores de Compose**  
  Si dejaste archivos `ui/theme` de Compose, activá Compose y sus dependencias.  
  Si no usás Compose, eliminá esos archivos.

---

## 🏷️ Renombrar el proyecto

- Nombre del proyecto: `settings.gradle.kts` → `rootProject.name = "TuNombre"`
- Nombre visible de la app: `@string/app_name` (por flavor o en `main`).
- Package / applicationId: refactor desde Android Studio + actualizar apps en Firebase y `google-services.json`.

---

## 📚 Licencia

Uso interno/demos. Adaptalo libremente a tus necesidades.
