package com.exploradordefractais

import kotlin.math.ln


fun itr_normalizada(Cx: Double, Cy:Double,
                    limiteDivergencia:Double,
                    maxIteracoes: Int,
                    SAMPLING_ITERACOES:Int): Long {


    var i: Int = 0
    var zx = Cx;
    var zy = Cy;
    var zx2 = zx * zx;
    var zy2 = zy * zy;
    //   if ((zx2+ zy2)>4.0) return 0;
    while (zx2 + zy2 < limiteDivergencia) {
        i++
        zy *= zx;
        zy += zy;
        zy += Cy;
        zx = zx2 - zy2 + Cx;
        zx2 = zx * zx;
        zy2 = zy * zy;
        if (i == maxIteracoes) return 0;
    }
    val log_zn = ln(zx2 + zy2) / 2.0

    val parcial =        SAMPLING_ITERACOES * ln(log_zn / 0.69314718056) / 0.69314718056; //parcela que falta para chegar no proximo i
    // if (parcial >SAMPLING_ITERACOES) parcial=SAMPLING_ITERACOES.toDouble();
    return (i * SAMPLING_ITERACOES - parcial).toLong();
}