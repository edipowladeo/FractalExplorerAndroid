package com.exploradordefractais.android

import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.exploradordefractais.FractalJanela
/** CONSOME AS TAFERAS DE PROCESSAMENTO DA JANELA DO FRACTAL*/

class ThreadProcessamento(val fractalJanela: FractalJanela):Thread() {
    val handler = Handler()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        try {
            while (true) {
                if (fractalJanela.executaTarefaDeProcessamentoComMaiorPrioridade()){
             //     ////Log.i("Thread", "running")
                } else {
                   // ////Log.i("Thread", "running idle")
                    sleep(15)

                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}