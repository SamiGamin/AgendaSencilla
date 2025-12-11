package com.SamiDev.agendasencilla.data.database

data class LlamadaLog(
    val numero: String,
    val nombre: String?, // Puede ser null si no está guardado
    val tipo: Int,       // 1: Entrante, 2: Saliente, 3: Perdida
    val fecha: Long,
    val duracion: String
)