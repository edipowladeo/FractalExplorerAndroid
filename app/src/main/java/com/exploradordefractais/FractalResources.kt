package com.exploradordefractais

import android.os.Build
import androidx.annotation.RequiresApi
import com.exploradordefractais.poolDeObjetos.PoolDeObjetos
import com.exploradordefractais.poolDeObjetos.PoolDeObjetosAlocaveis
import com.exploradordefractais.runnables.TarefaAlocarTexturas
import com.exploradordefractais.runnables.TarefaDesalocarTexturaGL
import com.exploradordefractais.runnables.TarefaPopularTexturaGL
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FractalResources {
    @RequiresApi(Build.VERSION_CODES.N)
    val tamSprite = Cvetor2i(64, 64)
    /** parâmetros do Bytebuffer de iteracoes e da textura OpenGL*/
    val entriesPerPixel = 4
    val bytesPerEntry = 1

    val tarefasPopularTextura =   ListaTarefas<TarefaPopularTexturaGL>()
    val tarefasAlocarTextura =    ListaTarefas<TarefaAlocarTexturas>()
    val tarefasDesalocarTextura = ListaTarefas<TarefaDesalocarTexturaGL>()

    val poolTexturas =
        PoolDeObjetosAlocaveis<TextureWrapperImpl>(
            fun(): TextureWrapperImpl {
                var textura = TextureWrapperImpl(tamSprite.x, tamSprite.y)
                tarefasAlocarTextura.add(
                    TarefaAlocarTexturas(
                        textura
                    )
                )
                return textura
            },
            10, 2000, 1L
        )


    val poolIteracoesByteBuffer =
        PoolDeObjetos<ByteBuffer>(
            fun(): ByteBuffer {
                return ByteBuffer.allocateDirect(
                    tamSprite.x * tamSprite.y * entriesPerPixel
                )
                    .order(ByteOrder.nativeOrder())
            },
            100, 2000, 1L
        )

    val poolIteracoesByteArray =
        PoolDeObjetos<ByteArray>(
            fun(): ByteArray {
                return ByteArray(tamSprite.x * tamSprite.y * bytesPerEntry * entriesPerPixel)
            },
            2, 20, 1L
        )

    val poolIteracoesArrayIteracoes =
        PoolDeObjetos<ArrayIteracoes>(
            fun(): ArrayIteracoes { return ArrayIteracoes(tamSprite.x * tamSprite.y) { 0 } },
            100, 2000, 1L
        )

    @RequiresApi(Build.VERSION_CODES.N)
    val janela = FractalJanela(this,tamSprite)

}