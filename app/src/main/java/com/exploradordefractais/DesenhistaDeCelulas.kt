package com.exploradordefractais


interface DesenhistaDeCelulas {
    fun desenharCelula(celula: Celula, escala:Float,posicao:CoordenadasTela)
    fun AtualizaUniforms(tempo:Float,escalaPaleta:Float)
}