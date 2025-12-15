package com.SamiDev.agendasencilla.data

/**
 * Modelo de datos que representa un contacto telefónico.
 * Unifica la información proveniente del sistema y el estado de "favorito" local.
 *
 * @property id ID único del contacto (proviene de Android).
 * @property nombreCompleto Nombre para mostrar del contacto.
 * @property numeroTelefono Número telefónico principal.
 * @property fotoUri URI de la foto del contacto (puede ser null).
 * @property esFavorito Indica si el contacto está marcado como favorito en la app.
 */
data class ContactoTelefono(
    val id: String,
    val nombreCompleto: String,
    val numeroTelefono: String,
    val fotoUri: String?,
    var esFavorito: Boolean = false
)