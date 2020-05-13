package com.exploradordefractais.runnables

import android.util.Log
import com.exploradordefractais.Celula

class TarefaPopularTexturaGL(val celula: Celula) : Runnable {
    override fun run() {
        if (!celula.flagMarcadaParaDestruicao and celula.flagMatrizIteracoesEstaAtualizada) {
            celula.textura.fillTextureFromBuffer(celula.iteracoesByteBuffer)
            celula.validarTextura()
            Log.i("tarefa popular","rodando")

        }
    }
}
