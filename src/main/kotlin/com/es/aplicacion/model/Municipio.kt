package com.es.aplicacion.model

data class Municipio(
    var CNUM: String?,
    val CPRO: String,
    val CUN: String,
    val DMUN50: String
){
    private var cont = 0

    init {
        CNUM = cont.toString()
    }
}