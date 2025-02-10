package com.es.aplicacion.dto

import com.es.aplicacion.model.Direccion

data class UsuarioRegisterDTO(
    val username: String,
    val email: String,
    var password: String,
    var passwordRepeat: String,
    val direccion: Direccion,
    val rol: String?
)
