package com.exploradordefractais.android

import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.exploradordefractais.FractalJanela

/** DESATIVADO POIS NAO HOUVE GANHO DE PERFORMANCE NOTAVEL
 * Passar a manipulação da janela para outra trhead deixou a trhead de desenho
 * esperando, tambem dificultou o controle de quando a manipulação atua,
 */
/** IDEALMENTE A MANIPULAÇÃO DEVE SER OTIMIZADA ATÉ QUE RODE A CADA FRAME SEM CAUSAR LAG*/

class ThreadManipularJanelas(val fractalJanela: FractalJanela):Thread() {
    val handler = Handler()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        try {
            while (true) {
                fractalJanela.atualizaCamadas();
            //    Log.i("thread manipular janelas", "running")
                }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}

/*
necessida de uma interface de runable que aceite argumento de objeto
ou precisa fazer override no momento da criação
class ThreadRodarEmLoop<T:Runnable>(val fractalJanela: FractalJanela):Thread() {
    val handler = Handler()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        try {
            while (true) {
                super.run()
                //     Log.i("thread manipular janelas", "running")
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}

 */