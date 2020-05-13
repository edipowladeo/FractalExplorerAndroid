package com.exploradordefractais

import com.exploradordefractais.runnables.TarefaAlocarTexturas
import com.exploradordefractais.runnables.TarefaPopularTexturaGL
import com.exploradordefractais.runnables.TarefaProcessamento
import java.util.concurrent.locks.ReentrantLock

/** CELULA É SUBCLASSE DE CAMADA*/
class Celula(val camada: Camada, val coordenadasPlano: CoordenadasPlano, val tamTextura: Cvetor2i) {


    var textura = camada.fractalJanela.poolTexturas.emprestaObjeto()

    val texturaLock = ReentrantLock()

    /**matriz de iteracoes*/ //TODO: remover transferencias de containers
    var iteracoes = camada.fractalJanela.poolIteracoesArrayIteracoes.emprestaObjeto()

    /**byteBuffer usado para criar texturas*/
    var iteracoesByteBuffer = camada.fractalJanela.poolIteracoesByteBuffer.emprestaObjeto()

    /**ativa caso a já possui todos recursos devolvidos para o pool
     * os recursos podem estar em uso por outra célula portanto nao devem ser utilizados*/
    var flagMarcadaParaDestruicao = false

    /**uma célula inativa não desenha na tela e nao processa iteracoes, porem permanece na memória*/
    var flagInativa = false

    /** ativa quando a textura estiver pronta para exibicao*/
    var flagPossuiTexturaValida = false

    var flagMatrizIteracoesEstaAtualizada = false

    init {
       camada.TarefasProcessamento.add(
           TarefaProcessamento(this)
       )
        camada.fractalJanela.qtdeCelulas++
    }

    //TODO: estudar todos os casos de uso da lock texturas
    fun BindTexture(){
        texturaLock.lock()
        textura.bind()
        texturaLock.unlock()
    }

    fun possuiTexturaValida():Boolean
    {
        return flagPossuiTexturaValida
    }

    /** usado caso o contexto opencl seja resetado*/
    fun solicitarGeracaoDeTexturaGL(){
        camada.fractalJanela.tarefasAlocarTextura.add(TarefaAlocarTexturas(this.textura))
        camada.fractalJanela.tarefasPopularTextura.add(TarefaPopularTexturaGL(this))
    }

    fun ProcessaMandelbrotECriaTextura(){
        PopulateLocalArrayWithFractal()
        transfereIteracoesParaBytebuffer()
        camada.fractalJanela.tarefasPopularTextura.add(
            TarefaPopularTexturaGL(
                this
            )
        )
    }

    @ExperimentalUnsignedTypes
    fun transfereIteracoesParaBytebuffer(){
//TODO: APENAS PARA TESTE, muito overload
        val poolIteracoesByteArray = camada.fractalJanela.poolIteracoesByteArray

        val byteArray= poolIteracoesByteArray.emprestaObjeto().also{
            val entriesPerPixel =camada.fractalJanela.entriesPerPixel
            iteracoes.forEachIndexed{indice,valor ->
                val indiceNaTexutraByte = indice * entriesPerPixel
                var valorint = valor.toLong()
                //    valorint +=250u
                it[indiceNaTexutraByte] = valorint.toByte()
                valorint /= 256
                it[indiceNaTexutraByte+1] = valorint.toByte()
                valorint /= 256
                it[indiceNaTexutraByte+2] = valorint.toByte()
                valorint /= 256
                it[indiceNaTexutraByte+3] = valorint.toByte()
            }
            iteracoesByteBuffer.position(0)
            iteracoesByteBuffer.put(it).position(0)
        }
        poolIteracoesByteArray.devolveObjeto(byteArray)
    }

    fun PopulateLocalArrayWithFractal() {
        val delta = camada.Delta
        for (j in 0 until tamTextura.y) {
          for (i in 0 until tamTextura.x) {
                val pidex = i + j * tamTextura.x
                iteracoes[pidex] = itr_normalizada(
                    coordenadasPlano.x + i * delta,
                    coordenadasPlano.y + j * delta,
                    camada.fractalJanela.limiteDivergencia,
                    camada.fractalJanela.maxIteracoes,
                    camada.fractalJanela.samplingIteracoes
                ).toInt()
            }
        }
    }

    fun liberaRecursos(){
        camada.fractalJanela.qtdeCelulas --
      //  Log.i("Celula","Qtde Celulas: ${camada.fractalJanela.qtdeCelulas} tam pool ${camada.fractalJanela.poolTexturas.getSize()} texturas gl alocadas ${TextureWrapperImpl.totalObjetosAlocados}")
        flagMarcadaParaDestruicao = true
        camada.fractalJanela.apply {
            poolTexturas.devolveObjeto(textura)
            poolIteracoesArrayIteracoes.devolveObjeto(iteracoes)
            poolIteracoesByteBuffer.devolveObjeto(iteracoesByteBuffer)

        }
    }

    fun validarTextura() {
        flagPossuiTexturaValida = true;
    }
}
