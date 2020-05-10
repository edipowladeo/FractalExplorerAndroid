package com.exploradordefractais.android

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.exploradordefractais.FractalJanela
import com.exploradordefractais.fractal.R

/** NA VERSAO BETA ESTA ATIVIDADE ESTA INATIVA */
/** HARDCODED EXIBINDO DIRETO A AVIDADE OPENGL*/

class MainActivity : AppCompatActivity() {

   @RequiresApi(Build.VERSION_CODES.N)
   var fractalJanela: FractalJanela? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start()
    }


    fun startExample01(view: View) {
       // val intent = Intent(this, Example01Activity::class.java)

        startActivity(intent)        // Do something in response to button
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun startExample02(view: View) {
        start()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun start(){
        val intent = Intent(this, ActivityExibirGLView::class.java)

      //  fractalJanela = FractalJanela()
    //    intent.putExtra("fractalJanela",fractalJanela)

        startActivity(intent)        // Do something in response to button

    }
}