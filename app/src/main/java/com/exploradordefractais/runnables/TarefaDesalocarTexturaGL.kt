package com.exploradordefractais.runnables

import com.exploradordefractais.TextureWrapper

//TODO: estudar qyal a  melhor dorma de injetar dependencia, pois a textura precisa criar uma tarefa que a auto destrua
class TarefaDesalocarTexturaGL (val textura: TextureWrapper) : Runnable {
    override fun run(){
        textura.desalocarTexturaGL()

    }
}
