package com.exploradordefractais

import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.math.abs
import kotlin.math.sqrt
/** INATIVO
 * usado para testar a implementação das texturas opengl
 * esta função recebe um pard de coordenadas cartesianas e retorna uma cor
 * funcionamento parecido com um fragment shader
 * pode ser usado para debug das células, mas seria ideal usar um fragment shader para fazer estes desenhos*/

//TODO: isto é realmente necessario?

@RequiresApi(Build.VERSION_CODES.O)
class dummyTexture:(Float, Float)-> TipoCor {
    override fun invoke(x:Float, y:Float): TipoCor {
        var r = 1f
        var g = 1f
        var b = 1f

        if (abs(y)> 0.45) r = 0.5f+ abs(x) // desenha um retângulo colorido
        if (abs(x)> 0.45) g = 0.5f+ abs(y)

        val dist = sqrt(x*x+y*y)

        if (abs(dist-0.35)<0.04){ // desenha um cículo azul
            r = 0f
            g=0.6f
        }

        return TipoCor(r, g, b)
    }
}