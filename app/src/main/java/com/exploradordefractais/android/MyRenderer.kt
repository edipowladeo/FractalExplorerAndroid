package com.exploradordefractais.android

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import com.exploradordefractais.*
import com.exploradordefractais.runnables.TarefaPopularTexturaGL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


public class MyRenderer(var context: Context) : GLSurfaceView.Renderer, DesenhistaDeCelulas {


    var textoDebug = StringBuilder()
    var ouvinte: CustomEventListener? = null

    fun setaOuvinte(Ouvinte: CustomEventListener) {
        ouvinte = Ouvinte
    }

    var fractalJanela: FractalJanela? = null
    //  lateinit var janelaP: FractalJanela.Propriedades

    var mProgramHandle = 0
    var mTextureUniformHandle = 0
    var mWindowSizeUniformHandle = 0


  //  val ThreadManipularJanelas = ThreadManipularJanelas(this)
  //  ThreadManipularJanelas.start()

    private val mVertices: FloatBuffer
    private val mBytesPerFloat = 4
    private val mStrideBytes = 2 * mBytesPerFloat
    private val mPositionOffset = 0
    private val mValuesPerVertice = 2

    internal var handlePosicaoVertices = 0
    internal var handlePosicaoCelulas = 0
    internal var handleTempo = 0
    internal var handleEscalaDaPaleta = 0

    init {
        val verticesData = floatArrayOf(
            // X, Y, Z,
            0.0f, 0.0f,
            0.0f, -1.0f,
            1.0f, 0.0f,
            1.0f, -1.0f
        )
        mVertices = ByteBuffer.allocateDirect(verticesData.size * mBytesPerFloat)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mVertices.put(verticesData)!!.position(0)
    }

    override fun desenharCelula(celula: Celula, escala:Float, posicao: CoordenadasTela) {
        celula.BindTexture()
        GLES20.glUniform3f(
            handlePosicaoCelulas,
            posicao.x.toFloat(),
            -posicao.y.toFloat(),
            escala)
        drawRectangle()
    }

    override fun AtualizaUniforms(tempo:Float,escalaPaleta:Float){
        GLES20.glUniform1f(handleEscalaDaPaleta,escalaPaleta)
        GLES20.glUniform1f(handleTempo,tempo)
    }

    private lateinit var myGlProgram: MyGLProgram

    init {
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        myGlProgram = MyGLProgram(
            context,
            "shaders/TextureWrapperVertex.glsl",
            "shaders/RawTextureFragment.glsl"
        )
        // Set the background clear color to gray.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        mProgramHandle = myGlProgram.getProgramHandle()
        handlePosicaoVertices = GLES20.glGetAttribLocation(mProgramHandle, "a_RectangleVertices");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mWindowSizeUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_WindowSize");
        handlePosicaoCelulas = GLES20.glGetUniformLocation(mProgramHandle, "u_SpritePosition")
        handleEscalaDaPaleta = GLES20.glGetUniformLocation(mProgramHandle, "u_escalaPaleta")
        handleTempo = GLES20.glGetUniformLocation(mProgramHandle, "u_tempo")

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(myGlProgram.getProgramHandle());
        //redundante???
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mVertices.position(mPositionOffset)
        GLES20.glVertexAttribPointer(
            handlePosicaoVertices, mValuesPerVertice, GLES20.GL_FLOAT, false,
            mStrideBytes, mVertices
        )
        GLES20.glEnableVertexAttribArray(handlePosicaoVertices)
    }

    inline fun drawRectangle() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        fractalJanela?.run {
            var tempoinicio = SystemClock.uptimeMillis()
            if (ExibirInformacoesDaJanelaEmOverlay) atualizarTexto();

            if(flagCameraEstaMovendo)atualizaCamadas();
            desenharCelulas(this@MyRenderer)
           // Log.i("Tempo", "Render ${SystemClock.uptimeMillis()-tempoinicio} millisseconds")

            /** Quando precisa executar uma TarefaPopularTexturaGL,
             *  é preciso verificar se a textura foi alocada*/
            fun ListaTarefas<TarefaPopularTexturaGL>.pollandRun(){
                poll()?.run{
                    if (celula.textura.alocadoComSucesso())  run() //só popula a textura se ela estiver alocada
                    else tarefasPopularTextura.add(this)// joga para o final da fila se não estiver alocado
                }
            }

            /**executa tarefas de alocar e desalocar texturas durante X milisegundos*/
            var numTarefasRealizadas =0
            var tempototal:Long = 0
            var tempoMs:Int =1000/FPSalvoAoManipular


            tarefasDesalocarTextura.run { for (i in 0 until size) poll()?.run()}
            while ((tempototal<tempoMs) and (!tarefasAlocarTextura.isEmpty()))
            {
                tempototal = SystemClock.uptimeMillis() - tempoinicio
                numTarefasRealizadas++
                tarefasAlocarTextura.poll()?.run()
                tarefasPopularTextura.pollandRun()
            }
            if (!flagCameraEstaMovendo){
                tarefasAlocarTextura.run { for (i in 0 until size) poll()?.run()}
                tarefasPopularTextura.run { for (i in 0 until size) pollandRun()}
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun atualizarTexto() {
        textoDebug.clear()
        fractalJanela?.let {
            textoDebug.append(it.toString())
        }
     //   ouvinte?.recieveText(textoDebug.toString())

        /* Thread(Runnable {
          ouvinte?.recieveText(textoDebug.toString())

      }).start()*/
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)


        fractalJanela?.let{
        it.setDimensaoDaJanelaDeSaida(
            CoordenadasTela(
                width.toDouble(),
                height.toDouble()
            )
        )
            //Log.i("Renderer", "fractalJanela ${largura} x ${altura}")

        }
        val ratio = width.toFloat() / height
        GLES20.glUniform2f(mWindowSizeUniformHandle,width.toFloat(),height.toFloat())

/** toda vez que onSurfaceChanged é chamado, há destruição do contexto OpenCL, e portanto de todas as texturas*/
/**como há persistência da fractalJanela, é preciso recriar todas as texturas*/
        fractalJanela?.run{
            camadas.forEach { (t, camada) ->
                camada.Celulas.forEachIndexed{i , colunas ->
                    colunas.forEachIndexed(){j, linhas ->
                        linhas.run{
                            solicitarGeracaoDeTexturaGL()
                        }
                    }
                }
            }
        }

    }


}
