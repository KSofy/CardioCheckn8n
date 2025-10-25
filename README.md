# CardioCheckn8n

# 🫀 CardioCheck App + n8n Integration

## 📱 Descripción del Proyecto
CardioCheck es una aplicación Android que permite a los usuarios registrar y monitorear su salud cardiovascular. Esta versión incluye una integración con n8n para automatizar el respaldo de datos y el envío de reportes por correo.

## 🔗 Integración con n8n
- Se utiliza un webhook para enviar datos desde la app a n8n.
- n8n registra los datos en Google Sheets, crea un reporte y envía un correo con el resumen.
- El flujo se activa automáticamente al guardar una nueva entrada en la app.

## 🛠️ Instrucciones de Instalación
1. Clona este repositorio.
2. Abre el proyecto en Android Studio.
3. Configura tu endpoint de n8n en el archivo `config.java`.
4. Ejecuta la app en tu dispositivo o emulador.

## 📂 Requisitos y Dependencias
- Android SDK 33+
- Retrofit para llamadas HTTP
- Permisos de Internet
- Cuenta de n8n con acceso a Google Sheets y Gmail

## 📸 Capturas de Pantalla
![Pantalla principal](screenshots/main.png)
![Flujo en n8n](screenshots/n8n-flow.png)

## 🎥 Video de la Fase 1
[Ver presentación inicial de la app](https://www.youtube.com/watch?v=TU_ENLACE_AQUI)

## 📄 Propuesta del Proyecto
La propuesta original está disponible en la carpeta `/propuesta`.

## 🔄 Flujo de n8n
El archivo `flujo-cardio-check.json` se encuentra en la carpeta `/n8n-flows`.

## 📅 Fecha de Entrega
25 de noviembre de 2025
