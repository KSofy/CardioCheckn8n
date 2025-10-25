# Cardio Check (Android, Java, Groovy Gradle)

Aplicación Android en Java para registrar presión arterial, obtener análisis con OpenAI y generar reportes PDF.

## Requisitos
- Min SDK 24, Target SDK 34
- Java 8 (compilación del proyecto), Android Studio con Gradle Wrapper

## Configuración inicial
1. Abre el proyecto en Android Studio.
2. Asegúrate de sincronizar Gradle (Groovy DSL). Si ves archivos `*.kts`, ya fueron migrados y neutralizados.
3. Ejecuta la app en un dispositivo/emulador con Internet.

## Uso
1. Bienvenida: Pulsa "Iniciar sesión" o "Registrarse".
2. Registro: Crea tu cuenta (nombre, email, contraseña >= 8).
3. Inicio de sesión: Ingresa email, contraseña y (opcional) tu clave de OpenAI. Puedes guardarla aquí.
4. Panel: 
   - "Registrar nueva medición" (FAB) para crear una lectura y obtener análisis IA.
   - "Asistente de IA" para chat en vivo.
   - "Ver historial" para listar, seleccionar y generar PDF.
5. PDF: En Historial selecciona mediciones y pulsa "Generar PDF". Luego compártelo por email (se usa FileProvider y almacenamiento privado: `getExternalFilesDir/Documents`).

## Clave OpenAI
- Se guarda en SharedPreferences (MODE_PRIVATE). No se incluye en el código.
- La app usa HttpURLConnection contra `https://api.openai.com/v1/chat/completions` (modelo `gpt-3.5-turbo`).

## Notas técnicas
- SQLite con `SQLiteOpenHelper` (`DatabaseHelper.java`), tablas `users` y `blood_pressure_readings`.
- PDF nativo con `PdfDocument`.
- IU con ConstraintLayout y Material.

## Solución de problemas
- Si Android Studio marca ids no encontrados en layouts, realiza un "Sync Project with Gradle Files" y limpia/reconstruye. Los layouts están en `app/src/main/res/layout/`.
- Si Gmail no aparece, se usa un chooser genérico de `ACTION_SEND`. Asegúrate de tener un cliente de email.


