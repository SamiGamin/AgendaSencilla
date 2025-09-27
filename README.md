# Agenda Sencilla

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)

Una aplicación de contactos para Android diseñada específicamente para adultos mayores, enfocándose en la simplicidad, accesibilidad y facilidad de uso.

## 🌟 Sobre el Proyecto

La tecnología moderna a menudo puede resultar abrumadora. "Agenda Sencilla" nace con el objetivo de eliminar esa barrera, ofreciendo una experiencia de gestión de contactos que prioriza la claridad visual sobre la complejidad funcional. En lugar de largas listas de texto y menús ocultos, la aplicación se centra en una interfaz gráfica con fotos grandes y acciones directas, haciendo que llamar o enviar un mensaje sea una tarea intuitiva y libre de estrés.

---

## 🖼️ Capturas de Pantalla

*(Consejo: Para añadir capturas, simplemente arrastra los archivos de imagen a la web de GitHub dentro de este archivo y se generará el enlace automáticamente.)*

| Pantalla Principal | Añadir Contacto |
| :---: | :---: |
| ![Pantalla Principal](ruta/a/tu/captura_principal.png) | ![Pantalla Añadir](ruta/a/tu/captura_añadir.png) |

---

## ✨ Características Principales

-   **📱 Interfaz Visual y Sencilla:** La pantalla principal es una cuadrícula de fotos grandes de los contactos favoritos.
-   **👆 Acciones con un Toque:** Botones grandes y claros debajo de cada contacto para **Llamar** y **Enviar Mensaje** de forma inmediata.
-   **👓 Alta Accesibilidad:**
    -   Textos de gran tamaño por defecto.
    -   Opciones en la configuración para aumentar aún más el tamaño de la fuente.
    -   Temas de color de alto contraste para una legibilidad óptima.
-   **💾 Persistencia Local:** Todos los contactos se guardan de forma segura en el dispositivo, por lo que la aplicación funciona perfectamente sin conexión a internet.
-   **➕ Gestión de Contactos Simplificada:** Un formulario de un solo paso para añadir o editar contactos, con campos grandes y etiquetas claras.

---

## 🛠️ Tecnologías y Arquitectura

Este proyecto está construido siguiendo las mejores prácticas recomendadas por Google para el desarrollo moderno de Android.

-   **Lenguaje:** **Kotlin** 100%.
-   **Arquitectura:** **MVVM (Model-View-ViewModel)** para una clara separación de responsabilidades.
-   **Interfaz de Usuario:**
    -   **XML** para la construcción de layouts.
    -   **View Binding** para un acceso seguro y eficiente a las vistas.
    -   **Material 3** para un diseño moderno y consistente.
-   **Base de Datos:** **Room Persistence Library** como capa de abstracción sobre SQLite.
-   **Navegación:** **Android Navigation Component** para gestionar el flujo entre fragments.
-   **Asincronía:** **Coroutines y Flow** para manejar operaciones en segundo plano de forma eficiente.

---

## 🚀 Cómo Compilar y Ejecutar

Para compilar y ejecutar el proyecto en tu máquina local, sigue estos sencillos pasos:

1.  **Clona el repositorio**
    ```sh
    git clone https://github.com/tu-usuario/Agenda-Sencilla.git
    ```
2.  **Abre el proyecto** en la última versión estable de Android Studio.
3.  **Sincroniza Gradle** para que descargue todas las dependencias necesarias.
4.  **Ejecuta la aplicación** en un emulador o en un dispositivo físico.

---

## 📄 Licencia

Distribuido bajo la Licencia MIT. Ver `LICENSE` para más información.
