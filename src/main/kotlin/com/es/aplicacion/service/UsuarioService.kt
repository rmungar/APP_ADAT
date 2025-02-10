package com.es.aplicacion.service

import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UsuarioService : UserDetailsService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var externalApiService: ExternalApiService

    override fun loadUserByUsername(username: String?): UserDetails {
        val usuario: Usuario = usuarioRepository
            .findByUsername(username!!)
            .orElseThrow {
                UnauthorizedException("$username no existente")
            }

        return User.builder()
            .username(usuario.username)
            .password(usuario.password)
            .roles(usuario.roles)
            .build()
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO) : UsuarioDTO? {
        var provinciaExistente = false
        var municipioDeProvincia = false
        if (usuarioInsertadoDTO.username.isBlank()) throw BadRequestException("El nombre del usuario no puede estar vacío.")
        if (usuarioInsertadoDTO.password.isBlank()) throw BadRequestException("La contraseña del usuario no puede estar vacía.")
        if (usuarioInsertadoDTO.email.isBlank()) throw BadRequestException("El email del usuario no puede estar vacío.")
        if (usuarioInsertadoDTO.passwordRepeat.isBlank()) throw BadRequestException("La contraseña del usuario no puede estar vacía.")
        if (usuarioInsertadoDTO.password != usuarioInsertadoDTO.passwordRepeat) throw BadRequestException("Las contraseñas ingresadas no coinciden.")
        if (usuarioInsertadoDTO.rol.isNullOrBlank()) throw BadRequestException("El rol del usuario no puede estar vacío o ser nulo.")
        if (usuarioInsertadoDTO.direccion.calle.isBlank()) throw BadRequestException("La calle no puede estar vacía.")


        val usuarioProv = usuarioInsertadoDTO.direccion.provincia.uppercase()
        val usuarioMunicipio = usuarioInsertadoDTO.direccion.municipio.uppercase()
        var CPRO = ""

        externalApiService.getProvinciasDeApi()?.data?.forEach {
            if (it.PRO == usuarioProv) {
                CPRO = it.CPRO
                provinciaExistente = true
            }
        }

        externalApiService.getMunicipiosApi(CPRO)?.data?.forEach {
            if (it.DMUN50 == usuarioMunicipio){
                municipioDeProvincia = true
            }
        }

        if (!provinciaExistente) throw BadRequestException("La provincia insertada no existe.")
        if (!municipioDeProvincia) throw BadRequestException("El municipio no pertenece a la provincia.")

        val usuarioAinsertar = Usuario(
            null,
            usuarioInsertadoDTO.username,
            passwordEncoder.encode(usuarioInsertadoDTO.password),
            usuarioInsertadoDTO.email,
            usuarioInsertadoDTO.direccion,
            usuarioInsertadoDTO.rol ?: "USER"
        )

        val usuarioExistente = usuarioRepository.findByUsername(usuarioAinsertar.username)
        if (usuarioExistente.isPresent) {
            throw BadRequestException("El usuario ya existe.")
        }
        usuarioRepository.insert(
            usuarioAinsertar
        )

        return UsuarioDTO(
            usuarioAinsertar.username,
            usuarioAinsertar.email,
            usuarioAinsertar.roles
        )

    }
}