package com.exploradordefractais.runnables

import com.exploradordefractais.Celula

class TarefaPopularTexturaGL(val celula: Celula) : Runnable {
    override fun run() {
        if (!celula.flagMarcadaParaDestruicao) {
            celula.textura.fillTextureFromBuffer(celula.iteracoesByteBuffer)
            celula.validarTextura()
        }
    }
}
