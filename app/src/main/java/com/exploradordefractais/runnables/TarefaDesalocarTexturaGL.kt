package com.exploradordefractais.runnables

import android.util.Log
import com.exploradordefractais.TextureWrapper

//TODO: estudar qyal a  melhor dorma de injetar dependencia, pois a textura precisa criar uma tarefa que a auto destrua
class TarefaDesalocarTexturaGL (val textura: TextureWrapper) : Runnable {
    override fun run(){
        Log.i("tarefa desalocar","rodando")
        textura.desalocarTexturaGL()

    }
}
