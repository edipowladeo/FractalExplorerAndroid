package com.exploradordefractais

import android.os.Build
import androidx.annotation.RequiresApi
import com.exploradordefractais.runnables.TarefaProcessamento

/** CAMADA É SUBCLASSE DE JANELA
Os vetores coordCelulasX(CX) e Os coordCelulasY(CY) armazendam as coordenadas no plano e na tela das células (c)
é uma otimização para evitar o cálculo repetido destas coordenadas, visto que são iguais para colunas e linhas
As coordenadas no plano são copiadas paras as células por valor , visto que são constantes.
As coordenadas na tela da célula são atualizadas somento nos vetores e consultadas no momento do desenho
//TODO: Otimizar mais, ainda está lento para rodar na thread de desenho


        CX0 CX1 CX2 CX3

 CY0    c00 c10 c20 c30
 CY1    c01 c11 c21 c31
 CY2    c02 c12 c22 c32
 CX3    c03 c13 c23 c33

A matriz de células é implementada usando uma Lista (colunas) de lista (linhas) de células

Esta classe possui métodos para inserir e remover linhas e colunas,
estes métodos manipulam e os vetores coordCelulas e a matriz de células ao mesmo tempo
 */

//TODO: Todos os métodos da camada estão feios, usar soluções mais elegantes

@RequiresApi(Build.VERSION_CODES.N)
class Camada(val fractalJanela: FractalJanela, coordenadasIniciais: CoordenadasPlanoEDelta)
{
   enum class Direcao {
        INCREMENTO, DECREMENTO
    }
    /**lista de tarefas*/
    val TarefasProcessamento =
        ListaTarefas<TarefaProcessamento>()

    /**Vetor de Coordenadas das células*/
    private var coordCelulasX = MutableList<CoordenadaPlano>(1){coordenadasIniciais.x}
    private var coordCelulasY = MutableList<CoordenadaPlano>(1){coordenadasIniciais.y}

    var posicaoRelativa = Cvetor2i(0,0)

    internal val Delta: TipoDelta = coordenadasIniciais.Delta

    /**Lista (colunas) de lista (linhas) de células*/
    internal var Celulas = MutableList<MutableList<Celula>>(1){MutableList(1){
        Celula(
            this,
            coordenadasIniciais,
            fractalJanela.tamSprite
        )
    }
    }

    init{
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun atualizaMatrizDeCelulas(multiplicadorTamJanela:Double) {
        val escala = 0.5*fractalJanela.fator_debug*multiplicadorTamJanela
        var janela_desenho = CoordenadasTela(
            fractalJanela.dimJanelaDeSaida.x * escala,
            fractalJanela.dimJanelaDeSaida.y * escala
        )

        val coord_max = fractalJanela.getCoordenadasPlano(janela_desenho)
        janela_desenho.x=- janela_desenho.x
        janela_desenho.y=- janela_desenho.y
        val coord_min = fractalJanela.getCoordenadasPlano(janela_desenho)

        //TODO:usar tipos e operator overload para melhorar a redabilidade
        //TODO: Melhorar para uma solução mais elegante
        var larguraPlano = fractalJanela.tamSprite.x.toDouble() * Delta
        var alturaPlano = fractalJanela.tamSprite.y.toDouble() * Delta

        val coord_min_alocar = CoordenadasPlano(
            coord_min.x,
            coord_min.y
        )
        val coord_max_alocar = CoordenadasPlano(
            coord_max.x - larguraPlano,
            coord_max.y - alturaPlano
        )

        val coord_min_desalocar = CoordenadasPlano(
            coord_min.x - larguraPlano,
            coord_min.y - alturaPlano
        )
        val coord_max_desalocar = CoordenadasPlano(
            coord_max.x,
            coord_max.y
        )

        while (coordCelulasX.first() > coord_min_alocar.x) adicionarColuna(
            Direcao.DECREMENTO
        )
        while (coordCelulasX.last() < coord_max_alocar.x) adicionarColuna(
            Direcao.INCREMENTO
        )
        while (coordCelulasY.first() > coord_min_alocar.y) adicionarLinha(
            Direcao.DECREMENTO
        )
        while (coordCelulasY.last() < coord_max_alocar.y) adicionarLinha(
            Direcao.INCREMENTO
        )

           if (coordCelulasX.size > 1){
        if (coordCelulasX.first() < coord_min_desalocar.x) removerColuna(
            Direcao.DECREMENTO
        )
    }
        if (coordCelulasX.size > 1){
        if (coordCelulasX.last() > coord_max_desalocar.x)         removerColuna(
            Direcao.INCREMENTO
        )
        }
        if (coordCelulasY.size > 1){
         if (coordCelulasY.first() < coord_min_desalocar.y)         removerLinha(
             Direcao.DECREMENTO
         )
        }
        if (coordCelulasY.size > 1){
        if (coordCelulasY.last() > coord_max_desalocar.y)         removerLinha(
            Direcao.INCREMENTO
        )
        }
    }

    fun getCoordenadasPlano(): CoordenadasPlano {
        return CoordenadasPlano(
            coordCelulasX.first(),
            coordCelulasY.first()
        )
    }

    fun getCoordenadasPlanoEDelta(): CoordenadasPlanoEDelta {
        return CoordenadasPlanoEDelta(
            getCoordenadasPlano(),
            Delta
        )
    }

    //TODO: refatorar nomes
    fun adicionarColuna(direcao: Direcao){
        val tamY = coordCelulasY.size
        val tamX = coordCelulasX.size

        if (direcao == Direcao.DECREMENTO) posicaoRelativa.x --

        val indiceX = if (direcao== Direcao.INCREMENTO)  tamX else 0

        val novacoordenadaOffset = if (direcao== Direcao.INCREMENTO) (tamX*Delta*fractalJanela.tamSprite.x) else (-Delta*fractalJanela.tamSprite.x)

        /** incrementa o vetor de coordenadas */
        val novaCoordenadaX = coordCelulasX.first() + novacoordenadaOffset
        coordCelulasX.add(indiceX,novaCoordenadaX)

        /**cria uma nova coluna */
        val novaColuna = emptyList<Celula>().toMutableList()

        /**Popula a coluna criada com uma linha para cada coordenada Y*/
        coordCelulasY.forEachIndexed { indiceY, it ->
            val novaCelula = Celula(
                this, CoordenadasPlano(
                    coordCelulasX[indiceX],
                    coordCelulasY[indiceY]
                ),
                fractalJanela.tamSprite
            )
            novaColuna.add(novaCelula)
        }

        /**Insere nova Coluna na matriz de células */
        Celulas.add(indiceX,novaColuna)
    }


    fun adicionarLinha(direcao: Direcao){
        val tamY = coordCelulasY.size
        val tamX = coordCelulasX.size

        if (direcao == Direcao.DECREMENTO) posicaoRelativa.y --

        val indiceY = if (direcao== Direcao.INCREMENTO)  tamY else 0

        val novacoordenadaOffset = if (direcao== Direcao.INCREMENTO) (tamY*Delta*fractalJanela.tamSprite.y) else (-Delta*fractalJanela.tamSprite.y)

        /** incrementa o vetor de coordenadas */
        val novaCoordenadaY =coordCelulasY.first() + novacoordenadaOffset
        coordCelulasY.add(indiceY, novaCoordenadaY)

        /** para cada coluna ...*/
        coordCelulasX.forEachIndexed{indiceX, it ->
            /** ... cria uma nova célula*/
            val novaCelula = Celula(
                this,
                CoordenadasPlano(
                    coordCelulasX[indiceX],
                    coordCelulasY[indiceY]
                ),
                fractalJanela.tamSprite
            )
            Celulas[indiceX].add(indiceY,novaCelula)
        }
    }


    fun removerColuna(direcao: Direcao){
        val tamY = coordCelulasY.size
        val tamX = coordCelulasX.size

        if (direcao == Direcao.DECREMENTO) posicaoRelativa.x ++

        val indiceX = if (direcao== Direcao.INCREMENTO)  tamX-1 else 0

        /** remove Coordenadas do vetor*/
        coordCelulasX.removeAt(indiceX)

        /** liberar recursos das células*/
        Celulas[indiceX].forEach {celula ->
            celula.liberaRecursos()
        }

        /** remove coluna*/
        Celulas.removeAt(indiceX)
    }

    fun removerLinha(direcao: Direcao) {
        val tamY = coordCelulasY.size
        val tamX = coordCelulasX.size

        if (direcao == Direcao.DECREMENTO) posicaoRelativa.y ++

        val indiceY = if (direcao == Direcao.INCREMENTO) tamY - 1 else 0

        /** remove Coordenadas do vetor*/
        coordCelulasY.removeAt(indiceY)

        /** liberar recursos das células*/
        Celulas.forEach { linha ->
            linha[indiceY].liberaRecursos()
        }

        /** remove linha*/
        Celulas.forEach { linha ->
            linha.removeAt(indiceY)
        }
    }

    fun liberarRecursos(){
        Celulas.forEach { linha ->
            linha.forEach{celula->
                celula.liberaRecursos()
            }
        }
    }

    override fun toString(): String {
       return super.toString()
    }


}