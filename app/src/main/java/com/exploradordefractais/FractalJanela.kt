package com.exploradordefractais

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.os.SystemClock
import androidx.annotation.RequiresApi
import com.exploradordefractais.poolDeObjetos.PoolDeObjetos
import com.exploradordefractais.poolDeObjetos.PoolDeObjetosAlocaveis
import com.exploradordefractais.runnables.TarefaAlocarTexturas
import com.exploradordefractais.runnables.TarefaDesalocarTexturaGL
import com.exploradordefractais.runnables.TarefaPopularTexturaGL
import java.lang.Math.pow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.floor
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.truncate

/** OBJETO PRINCIPAL*/
/** UMA JANELA POSSUI CAMADAS QUE POR SUA VEZ POSSUEM CÉLULAS*/

/** Thread Safe
 * Lock na fractalJanela toda
 * locks nas filas de tarefas
 * tarefa deve conter todas as informacoes para realizar
 * tarefas sao executadas por outras trheads
 * TODO:lock nas camadas caso queira criar thread exclusiva de adicionar/remover ,
 * somente caso esteja muito lento (profiling) seria a ultima coisa a fazer
  */
@RequiresApi(Build.VERSION_CODES.N)
class FractalJanela() : Parcelable, FractalJanelaPropriedades() {

    var qtdeCelulas = 0;

    val texturaPlaceholder = TextureWrapperImpl(64, 64)  // inativo

    val tarefasPopularTextura =   ListaTarefas<TarefaPopularTexturaGL>()
    val tarefasAlocarTextura =    ListaTarefas<TarefaAlocarTexturas>()
    val tarefasDesalocarTextura = ListaTarefas<TarefaDesalocarTexturaGL>()

    val lock = ReentrantLock()

    /**VARIAVEIS DE ESTADO */
    var dimJanelaDeSaida =   CoordenadasTela(1000.0, 1800.0)

    var posicaoCameraAtual = PosicaoCameraInicial
    var PosicaoCameraUltimoFrame = CoordenadasPlanoEDelta(CoordenadasPlano(0.0,0.0),0.0)
    var PosicaoCameraDesejada = posicaoCameraAtual
    var flagCameraEstaMovendo = false;

    var coordenadasDaJanelaPlano = CoordenadasPlano(0.0,0.0)


    val poolTexturas =
        PoolDeObjetosAlocaveis<TextureWrapperImpl>(
            fun(): TextureWrapperImpl {
                var textura = TextureWrapperImpl(tamSprite.x, tamSprite.y)
                this.tarefasAlocarTextura.add(
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

    /** key da camada é o valor de magnificação*/
    /** quanto maior, menor é o tamanho aparente da camada e portanto maior resolução  qualidade aparente */
    /** valores menores representam baixa resolucao, e são processados e desenhados primeiro e portanto ficam atrás*/
    var camadas: SortedMap<Int, Camada> = emptyMap<Int, Camada>().toSortedMap()

    constructor(parcel: Parcel) : this() {}

    init {
   //     texturaPlaceholder.populateByteArrayUsingDrawerFunction()
//        TarefasAlocarTextura.add(velhaTarefaCriarTexturaGL(texturaPlaceholder))

        atualizaCamadas()

  //      val ThreadManipularJanelas = ThreadManipularJanelas(this)
    //    ThreadManipularJanelas.start()
        //TODO: Cria thread, porém se o objeto fractalJanela sai de escopo, thread fica solta
    }

    fun moverCamera(dXpixels: Float, dYpixels: Float) {
        lock.lock()
        posicaoCameraAtual.x += dXpixels * posicaoCameraAtual.Delta * FatorScroll
        posicaoCameraAtual.y += dYpixels * posicaoCameraAtual.Delta * FatorScroll
        PosicaoCameraDesejada = posicaoCameraAtual;
        lock.unlock()
    }

    fun modificarMagnificacaoCamera(fator: Float) {
        lock.lock()
        posicaoCameraAtual.Delta /= fator.pow(fatorEscala)
        PosicaoCameraDesejada = posicaoCameraAtual
        lock.unlock()
    }

    /** injecao de dependencia */
    fun setDimensaoDaJanelaDeSaida(dimensao: CoordenadasTela) {
        lock.lock()
       dimJanelaDeSaida = dimensao
        lock.unlock()
    }

    fun getDimensaoDaJanelaDeSaida(): CoordenadasTela {
        return dimJanelaDeSaida
    }

    /** converte coordenadas na tela para coordenadas no plano mandelbrot*/
    fun getCoordenadasPlano(coordenadasTela: CoordenadasTela): CoordenadasPlano {
        return CoordenadasPlano(
            posicaoCameraAtual.x + coordenadasTela.x * posicaoCameraAtual.Delta * 2,
            posicaoCameraAtual.y + coordenadasTela.y * posicaoCameraAtual.Delta * 2
        )
    }

    fun atualizaCamadas() {
        lock.lock()
        adicionarRemoverCamadas()
        //atualizaIntervaloAlocacao()
        camadas.values.forEachIndexed() { index,camada ->
            lock.unlock() //Deixa outras threads usarem a fractalJanela
            lock.lock()
            //TODO :melhorar tamanho dinâmico das camadas
            val escala = if (index ==0) 2.0 else 1.0 // dobra o tamanho de desenho da primeira camada, para evitar mostrar o fundo
           camada.atualizaMatrizDeCelulas(escala)
        }
        lock.unlock()
    }

/**As coordenadas da célula são calculadas em relativas à célula*/
/** Assim, a cada frame apenas uma operacao envolvendo coordenadas plano(lenta) é realizada*/
    fun desenharCelulas(desenhista: DesenhistaDeCelulas){
        lock.lock()
        flagCameraEstaMovendo = false
//TODO: estudar shallow copy, deep copy e comparadores
        if(PosicaoCameraUltimoFrame.x != posicaoCameraAtual.x) flagCameraEstaMovendo = true
        if(PosicaoCameraUltimoFrame.y != posicaoCameraAtual.y) flagCameraEstaMovendo = true
        if(PosicaoCameraUltimoFrame.Delta != posicaoCameraAtual.Delta) flagCameraEstaMovendo = true

            PosicaoCameraUltimoFrame.x = posicaoCameraAtual.x
            PosicaoCameraUltimoFrame.y = posicaoCameraAtual.y
            PosicaoCameraUltimoFrame.Delta = posicaoCameraAtual.Delta

        AtualizaPosicaoDaJanela()
     //   atualizaCameraECamadas()
        val time = SystemClock.uptimeMillis() % 10000L
        val angleInRad = 6.28318530f / 10000.0f * time.toFloat() * velocidadeCircularCores
        //angulo é usado pelpo shader para animar a paleta(circular cores)
        desenhista.AtualizaUniforms( angleInRad,escalaPaleta)

        val coordenadasTelaJanela : CoordenadasTela = CoordenadasTela(
            (coordenadasDaJanelaPlano.x-posicaoCameraAtual.x) / posicaoCameraAtual.Delta,
            (coordenadasDaJanelaPlano.y-posicaoCameraAtual.y) / posicaoCameraAtual.Delta
        )
        camadas.forEach { (t, camada) ->
            val escalaX =camada.Delta * tamSprite.x / posicaoCameraAtual.Delta//escala das celulas
            val escalaY =camada.Delta * tamSprite.y / posicaoCameraAtual.Delta
            val posicaorelativaI = camada.posicaoRelativa.x
            val posicaorelativaJ = camada.posicaoRelativa.y
            camada.Celulas.forEachIndexed{i,colunas ->
                colunas.forEachIndexed{j, linhas ->
                    linhas.run {
                        val posicao = CoordenadasTela( //posicao de cada célula
                            coordenadasTelaJanela.x + (i+posicaorelativaI)*escalaX,
                            coordenadasTelaJanela.y + (j+posicaorelativaJ)*escalaY)
                        if (this.possuiTexturaValida()) {
                          if (this.flagMatrizIteracoesEstaAtualizada)  desenhista.desenharCelula(this,escalaX.toFloat(),posicao)
                        }
                    }
                }
            }
        }
        lock.unlock()
    }

/** os valores de posicaorelativa dão overflow caso não Se mude a posicao pivo da fractalJanela periodicamente*/
    fun AtualizaPosicaoDaJanela() {
        val Delta = camadas.values.first().Delta
        val nCelulasDeslocarx = truncate(floor((posicaoCameraAtual.x-coordenadasDaJanelaPlano.x) / (Delta*tamSprite.x)) + 0.5).toInt();
        val nCelulasDeslocary = truncate(floor((posicaoCameraAtual.y-coordenadasDaJanelaPlano.y) / (Delta*tamSprite.y)) + 0.5).toInt();

        this.coordenadasDaJanelaPlano.x += nCelulasDeslocarx*Delta*tamSprite.x;
        this.coordenadasDaJanelaPlano.y += nCelulasDeslocary*Delta*tamSprite.y;

        var passo = 1;
        camadas.values.forEach {camadas->
            camadas.posicaoRelativa.x -= nCelulasDeslocarx*passo
            camadas.posicaoRelativa.y -= nCelulasDeslocary*passo
            passo*=2
        }
    }

    fun executaTarefaDeProcessamentoComMaiorPrioridade():Boolean{
        lock.lock()
        camadas.values.forEach { camada ->
            camada.TarefasProcessamento.poll()?.run {
                lock.unlock()
                run()
                return true //executou tarefa
            }
        }
        lock.unlock();
        return false //camada não possui tarefas a executar
    }

    private fun atualizaIntervaloDesenho(){

    }

      private fun adicionarRemoverCamadas()    {
        val maiorvalor = log((1.0/(posicaoCameraAtual.Delta*minTamanhoAparentePixel)),2.0).toInt()
        val menorvalor = maiorvalor - quantidadeDeCamadasAlemDaPrincipal

        val camadasDesejadas = IntRange(
            menorvalor,
            maiorvalor
        )

        /** Todas as novas camadas são alinahdas a maior camada*/

        //TODO:Substituir por comparação direta com o range
        camadasDesejadas.forEach {
            //TODO: usar metodo .in
            if (!camadas.containsKey(it)){
                val coordenadas =
                    CoordenadasPlanoEDelta(
                        coordenadasDaJanelaPlano,
                        getDeltaFromIntegerMagnification(it)
                    )
                   // 1.0/512.0 )
                camadas.put(it,
                    Camada(this, coordenadas)
                )
            }//TODO:consertar delta
        }

          //TODO: Solução abaixo tá com cara de gambiarra
        /** cria uma lista de todas as camadas que devem ser apagadas*/
        var camadasApagar = emptyList<Int>().toMutableList()

        camadas.forEach{ key, camada ->
            if (key !in camadasDesejadas) {
                camadasApagar.add(key)
                camada.liberarRecursos()
            }
        }
            camadasApagar.forEach {
                camadas.remove(it)
        }

    }

    override fun toString(): String {
        lock.lock()
        val stringB = StringBuilder()
        stringB.append("Dimensoes da FractalJanela: " + dimJanelaDeSaida)
        stringB.append("\nFila Criar Textura " + tarefasAlocarTextura.size )
        stringB.append("\nFila Desalocar Textura " + tarefasAlocarTextura.size )
        //  textoDebug.append("\nmin:" +coord_min)
        //  textoDebug.append("\nmax:" +coord_max)
        stringB.append("\nCoord Camera Atual " + posicaoCameraAtual)
        stringB.append("\nCoord FractalJanela Plano " + coordenadasDaJanelaPlano)
      //  stringB.append("\nCoord FractalJanela Tela " + )
        camadas.forEach{camada ->
            stringB.append("\n\t ${camada.key} ${camada.value.posicaoRelativa}: Fila Processos "+camada.value.TarefasProcessamento.size)}
        lock.unlock()
        return stringB.toString()
    }

    private fun getDeltaFromIntegerMagnification(mag:Int): TipoDelta {
       return pow(0.5,mag.toDouble())
    }

    //TODO: estudar a implementacao da interface parcelable, está aqui para injetar objeto via intent
    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FractalJanela> {
        override fun createFromParcel(parcel: Parcel): FractalJanela {
            return FractalJanela(parcel)
        }

        override fun newArray(size: Int): Array<FractalJanela?> {
            return arrayOfNulls(size)
        }
    }


}