# Agenda Sencilla

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

**Agenda Sencilla** es una aplicación de gestión de contactos y llamadas para Android, diseñada meticulosamente pensando en la **simplicidad y la accesibilidad**. Su objetivo principal es facilitar el uso del teléfono a personas mayores o a cualquier usuario que prefiera una interfaz limpia, directa y sin distracciones.

---

## 🌟 Características Principales

### 👥 Gestión de Contactos Simplificada
*   **Integración Nativa**: Lee y muestra los contactos directamente de la agenda del teléfono.
*   **Búsqueda Rápida**: Barra de búsqueda intuitiva para encontrar contactos por nombre o número instantáneamente.
*   **Detalles Claros**: Visualización limpia de la información del contacto con opciones grandes para llamar.

### ⭐ Favoritos (Acceso Rápido)
*   **Lista de Prioridad**: Sección dedicada para los contactos más frecuentes.
*   **Gestión Local**: Posibilidad de marcar/desmarcar favoritos, guardando esta preferencia localmente (usando Room Database) sin afectar la agenda nativa del dispositivo.
*   **Búsqueda en Favoritos**: Filtra rápidamente dentro de tu lista de personas importantes.

### 📞 Marcador Inteligente (Smart Dialer)
*   **Teclas Grandes**: Diseñado para una fácil visualización y pulsación.
*   **Búsqueda T9 / Sugerencias**: Al marcar números, la app sugiere contactos coincidentes en tiempo real.
*   **Historial Integrado**: Acceso directo al registro de llamadas (Entrantes, Salientes, Perdidas) desde la misma pantalla de marcación.
*   **Código de Colores**: Identificación visual rápida del tipo de llamada (Verde: Entrante, Azul: Saliente, Rojo: Perdida).

### 👓 Accesibilidad y Personalización
*   **Lector de Voz (TTS)**: La app puede leer en voz alta el nombre del contacto antes de llamar, proporcionando una confirmación auditiva.
*   **Tamaño de Fuente Ajustable**: Configuración global para cambiar el tamaño del texto:
    *   *Normal*
    *   *Grande*
    *   *Más Grande*
*   **Temas**: Soporte completo para Tema Claro y Tema Oscuro (o seguir el sistema), optimizando la legibilidad en cualquier entorno de luz.

---

## 🛠️ Tecnologías y Arquitectura

El proyecto está construido siguiendo los estándares modernos de desarrollo Android (Modern Android Development - MAD).

*   **Lenguaje**: [Kotlin](https://kotlinlang.org/) (100%).
*   **Patrón de Arquitectura**: **MVVM** (Model-View-ViewModel) para separar la lógica de negocio de la UI y manejar el ciclo de vida de forma segura.
*   **Inyección de Dependencias**: Gestión manual eficiente de dependencias (Singleton para Base de Datos y Preferencias).
*   **Persistencia de Datos**:
    *   **Room Database**: Para almacenar la lista de favoritos localmente.
    *   **SharedPreferences**: Para guardar las preferencias de usuario (Tema, Tamaño de fuente, TTS).
*   **Programación Asíncrona**: **Coroutines** y **Kotlin Flow** (StateFlow, SharedFlow) para operaciones en segundo plano y gestión reactiva del estado de la UI.
*   **Interfaz de Usuario (UI)**:
    *   **XML Layouts** con **ViewBinding**.
    *   **Material Design 3**: Uso de componentes modernos como `MaterialButton`, `BottomNavigationView`, `FloatingActionButton`.
    *   **ConstraintLayout**: Para diseños flexibles y responsivos.
*   **Navegación**: **Jetpack Navigation Component** (Single Activity Architecture).
*   **Carga de Imágenes**: [Glide](https://github.com/bumptech/glide) para cargar y cachear fotos de contactos eficientemente.

---

## 🔐 Permisos Solicitados

Para funcionar correctamente como una agenda telefónica, la app requiere los siguientes permisos:

1.  `READ_CONTACTS`: Necesario para mostrar la lista de contactos del dispositivo.
2.  `CALL_PHONE`: Permite realizar llamadas directamente desde la app sin tener que abrir el marcador nativo.
3.  `READ_CALL_LOG`: Permite mostrar el historial de llamadas recientes en la pantalla de marcación.

*Nota: La aplicación maneja la solicitud de estos permisos en tiempo de ejecución, explicando al usuario si son necesarios.*

---

## 🚀 Instalación y Pruebas

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/tu-usuario/Agenda-Sencilla.git
    ```
2.  Abre el proyecto en **Android Studio**.
3.  Sincroniza el proyecto con Gradle.
4.  Ejecuta la app en un emulador o dispositivo físico.

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia MIT. Eres libre de usarlo, modificarlo y distribuirlo.

---
*Desarrollado con ❤️ para simplificar la tecnología.*
