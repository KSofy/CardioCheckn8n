# CardioCheckn8n

# 🫀 CardioCheck App + n8n Integration

## 📱 Descripción del Proyecto
CardioCheck es una aplicación Android que permite a los usuarios registrar y monitorear su salud cardiovascular. Esta versión incluye una integración con n8n para automatizar el respaldo de datos y el envío de reportes por correo.

## 🔗 Integración con n8n
- Se utiliza un webhook para enviar datos desde la app a n8n.
- n8n registra los datos en Google Sheets, genera un reporte y envía un correo con la medicion y si la medicion es alta se le envia un correo al Contacto de emergencia.
- El flujo se activa automáticamente al guardar una nueva entrada en la app.

## 🛠️ Instrucciones de Instalación
Paso 1: Obtener el Código Fuente1.Clona el repositorio de Git o descomprime el archivo del proyecto en una carpeta de tu elección.
Paso 2: Abrir el Proyecto en Android Studio1.Abre Android Studio.2.Selecciona "Open an existing Project" (Abrir un proyecto existente).3.Navega hasta la carpeta raíz del proyecto (la que contiene el archivo build.gradle) y selecciónala.4.Espera a que Android Studio termine de sincronizar el proyecto con Gradle. Esto puede tardar unos minutos la primera vez.
Paso 3: (Opcional) Configurar el Webhook de n8n La aplicación está configurada para enviar datos de cada nueva medición a un servicio de automatización (n8n) a través de un Webhook.1.Localiza la URL del Webhook:•Abre el archivo DashboardActivity.java.•Busca el método sendDataToN8n.2.Actualiza la URL:•Dentro de ese método, encontrarás una línea como esta:JavaString webhookUrl = "https://primary-production-7bc2e.up.railway.app/webhook/6b819410-23ab-4ee0-8e4b-2bdb3f2ab28a";•Reemplaza la URL entre comillas con la URL de tu propio Webhook de n8n si deseas utilizar esta funcionalidad. Si no, puedes dejarla como está (simplemente fallará silenciosamente sin afectar la app).Paso 5: Compilar y Ejecutar la Aplicación1.Conecta un dispositivo Android físico o inicia un emulador de Android.2.Asegúrate de que tu dispositivo/emulador está seleccionado en la barra de herramientas de Android Studio.3.Haz clic en el botón "Run 'app'" (el ícono de play verde).4.La aplicación se instalará y se iniciará en tu dispositivo. Ahora puedes registrar un nuevo usuario y comenzar a usarla.

## 📂 Requisitos y Dependencias
1. Requisitos de Software•IDE: Android Studio (versión "Hedgehog" 2023.1.1 o superior recomendada).•Lenguaje: Java.•SDK de Android:•minSdkVersion: 24 (Android 7.0 Nougat) o superior.•targetSdkVersion: 34 (Android 14).•Sistema de Compilación: Gradle.2. Dependencias Principales (Librerías)Estas librerías están definidas en el archivo build.gradle del módulo app.•UI y Componentes de Android (AndroidX):•androidx.appcompat:appcompat:1.6.1: Proporciona compatibilidad con versiones anteriores de Android para componentes de la interfaz de usuario.•androidx.constraintlayout:constraintlayout:2.1.4: Para la creación de layouts complejos y flexibles.•androidx.recyclerview:recyclerview:1.3.2: Para mostrar listas eficientes de datos (como el historial de mediciones).•androidx.cardview:cardview:1.0.0: Para mostrar información en tarjetas con sombras y esquinas redondeadas.•com.google.android.material:material:1.11.0: Proporciona componentes de Material Design (botones, campos de texto, diálogos, etc.).•Gráficos y Visualización:•com.github.PhilJay:MPAndroidChart:v3.1.0: Una potente librería para crear gráficos y diagramas, utilizada para mostrar la evolución de la presión arterial.•Red y Comunicación:•com.android.volley:volley:1.2.1: Una librería de red para realizar peticiones HTTP. Se utiliza para enviar datos al Webhook de n8n.3. APIs Externas y Servicios•OpenAI API:•Servicio: OpenAI Chat Completions API.•Modelo Utilizado: gpt-3.5-turbo.•Autenticación: Mediante Bearer Token (API Key).•Propósito: Generar análisis, consejos y respuestas a preguntas del usuario de forma personalizada, actuando como un asistente de salud virtual.•Implementación: OpenAIClient.java.•n8n (Opcional):•Servicio: Webhook HTTP POST.•Propósito: Recibir datos de nuevas mediciones en tiempo real para activar flujos de trabajo automatizados (ej. enviar notificaciones de alerta por email, guardar en una hoja de cálculo, etc.).•Implementación: Método sendDataToN8n en DashboardActivity.java.4. Configuraciones de Proyecto Necesarias•Permiso de Internet: La aplicación requiere acceso a internet para comunicarse con las APIs. El siguiente permiso debe estar presente en el archivo AndroidManifest.xml:Manifest<uses-permission android:name="android.permission.INTERNET" />Merge Into Manifest•Gestión de Claves Secretas: La clave de la API de OpenAI se gestiona de forma segura utilizando local.properties y BuildConfig, evitando que sea expuesta en el código fuente.
   
## 📸 Capturas de Pantalla
El flujo:
<img width="1820" height="882" alt="Captura de pantalla 2025-10-24 193518" src="https://github.com/user-attachments/assets/5bb6db84-1fb4-4bbe-b715-89b9c9bf0653" />
<img width="1119" height="858" alt="Captura de pantalla 2025-10-24 193430" src="https://github.com/user-attachments/assets/edcab51c-96a5-41f9-a560-ae008fe10286" />
<img width="1844" height="818" alt="Captura de pantalla 2025-10-24 193503" src="https://github.com/user-attachments/assets/d7e9b7b0-cf95-4aa7-bc9a-38cab03157c5" />
![Imagen de WhatsApp 2025-10-24 a las 19 36 21_0104584b](https://github.com/user-attachments/assets/1548610e-12d3-4124-95a1-dacafb8b84e3)
![alerta](https://github.com/user-attachments/assets/e3690ea2-b5df-46c4-b92f-5a654e85dee9)

## 🎥 Video de la Fase 1
Video de Sofia
https://youtu.be/mGvw6HAH9KE

## Json Del Flujo
- Se encuentra en CardioCheckjson.
  
## 📄 Propuesta del Proyecto Original
https://drive.google.com/file/d/12uhGbfld2qjqSHXQIIgwppWGOD-eyN0d/view?usp=drivesdk
>>>>>>> 27b855516167043da12a2354833df2b349a55dbd


