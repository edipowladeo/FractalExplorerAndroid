package com.exploradordefractais.runnables

import com.exploradordefractais.Celula

/** um objeto Tarefa deve possuir todas as informacoes para que a tarefa seja executada*/

class TarefaProcessamento(val celula: Celula): Runnable {
   override fun run(){
        if (!celula.flagMarcadaParaDestruicao)  {
            celula.ProcessaMandelbrotECriaTextura()
            celula.flagMatrizIteracoesEstaAtualizada = true
        }
    }
}



