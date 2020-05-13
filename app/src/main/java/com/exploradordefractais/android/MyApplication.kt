package com.exploradordefractais.android

import android.app.Application
import android.content.Context
import com.exploradordefractais.FractalResources

/** TODO FOR NEXT RELEASE:
 * TODO: regenAllTextures causes flickering at startup
 * TODO: separate rendering, processing, erasing regions.
 */

/** Bugs conhecidos*/
//TODO: BUG: Se der zoom até o final, ele cria milhares de tarefas.
//TODO: BUG: Shader só consegue ler 16 bits do valor da iteracão
//TODO: BUG: possuiTexturaValida? esta retornando true mesmo quando nao deveria
//TODO: BUG: se eu chamar tarefa desalocar antes de tarefa alocar, causa vazamento de memória

//TODO:Navegacao da camera com "mola" até local de interesse
//TODO:implementar celulas "intativas"
//TODO:Save user config in JSON file

//TODO:openCL
//TODO:paletas
//TODO:paletas no shader
//TODO:interpolacao no shader
//TODO:multisampling no shader
//TODO:editor de paletas
//TODO:multiprecisao
//TODO:Algoritmo "mágico" para multiprecisao rápida
//TODO:Salvar locais de Interesse
//TODO:Controle de versão

class MainApplication : Application() {

    companion object {
        private lateinit var instance: MainApplication
        val recursos=  FractalResources()
        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = applicationContext()
    }
}