package com.exploradordefractais.runnables

import com.exploradordefractais.TextureWrapper

class TarefaAlocarTexturas (val textura: TextureWrapper) : Runnable{
    override fun run() {
         textura.alocarTexturaGL()
    }
}
