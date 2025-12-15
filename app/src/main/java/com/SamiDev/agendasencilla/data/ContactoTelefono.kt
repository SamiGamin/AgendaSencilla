package com.SamiDev.agendasencilla.data

data class ContactoTelefono(
    val id: String,
    val nombreCompleto: String,
    val numeroTelefono: String,
    val fotoUri: String?,
    var esFavorito: Boolean = false
)