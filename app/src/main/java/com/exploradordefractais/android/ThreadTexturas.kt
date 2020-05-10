package com.exploradordefractais.android

import android.os.Build
import android.os.Handler
import androidx.annotation.RequiresApi
import com.exploradordefractais.FractalJanela

/** DESATIVADO!!*/
/** thread não pode ser executada, por que as manipulações de texturas devem ser feitas na thread GL*/

class ThreadTexturas(val fractalJanela: FractalJanela):Thread() {
    val handler = Handler()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        try {
            while (true) {
                fractalJanela.tarefasDesalocarTextura.poll()?.run();

                fractalJanela.tarefasAlocarTextura.poll()?.run();

                }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}