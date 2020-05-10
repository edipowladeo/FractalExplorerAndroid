package com.exploradordefractais

interface Alocavel {

    fun alocar()
    fun desalocar()

    fun alocadoComSucesso(): Boolean
}