package com.exploradordefractais.android

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.exploradordefractais.*
import com.exploradordefractais.fractal.R
import com.exploradordefractais.runnables.TarefaAlocarTexturas
import com.exploradordefractais.runnables.TarefaDesalocarTexturaGL
import java.io.File
import java.io.FileFilter
import java.util.regex.Pattern

//TODO: estudar injecao de parametros fractalJanela e glview e textview tipos nulaveis


@RequiresApi(Build.VERSION_CODES.N)
class ActivityExibirGLView() : CustomEventListener, AppCompatActivity() {

    /** usado para debug */
    var lista_texturas = emptyList<TextureWrapperImpl>().toMutableList()

    var textView: TextView? = null
    lateinit var glview: MyGLSurfaceView

    var resources = FractalResources()

    private lateinit var myGLSurfaceView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO: resolver bugs de orientaçao
      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /** Cria o fractal*/
  //      fractalJanela = FractalJanela()
        //fractalJanela = intent.getParcelableExtra("fractalJanela")

        setContentView(R.layout.layout_new)

        textView = this.findViewById(R.id.textView1)
        glview = this.findViewById<MyGLSurfaceView>(R.id.myGLSurfaceView1)
        glview.myRenderer.setaOuvinte(this)

        /** injeta dependência da fractalJanela no myRenderer*/
        glview.myRenderer.fractalResourcesResources = resources

        /**cria threads de processamento */
        Log.i("Cores", getNumberOfCores().toString())
        resources.janela.let { janela ->
            /**IMPLEMENTAÇÃO 1: CLASSE QUE HERDA TRHEAD*/
            val threadProcessamento =
                List<ThreadProcessamento>(4) { ThreadProcessamento(janela) }
            threadProcessamento.forEach { it.start() }

            /**IMPLEMENTAÇÃO 2: PROVER OBJETO RUNNABLE - NÃO TESTEI ESTA IMPLEMENTACAO*/
            /*
            val thread = Thread {
                while (true) {
                    /** codigo que vai rodar na thread*/
                    if (!fractalJanela.executaTarefaDeProcessamentoComMaiorPrioridade()) Thread.sleep(15)
                }
            }*/

            //TODO: executorService funciona, mas nao sei quais são as vantagens de usar ele ou threads
            //TODO : aprender a injetar o runnable na trhead sem usar interface
            /**IMPLEMENTAÇÃO 3: ExecutorService provendo runnable*/
            /*
            val executorService = Executors.newSingleThreadScheduledExecutor().apply {
                schedule(
                    Runnable {
                        while (true) {
                            /** codigo que vai rodar na thread*/
                            if (fractalJanela.executaTarefaDeProcessamentoComMaiorPrioridade()) {
                                //     ////Log.i("Thread", "running")
                            } else {
                                // ////Log.i("Thread", "running idle")
                                Thread.sleep(15)
                            }
                        }
                    },
                    1, TimeUnit.SECONDS
                )
            }*/
        }
    }

    private fun getNumberOfCores(): Int {
        if (Build.VERSION.SDK_INT >= 17) {
            return Runtime.getRuntime().availableProcessors()
        } else return getNumCoresOldPhones()
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    private fun getNumCoresOldPhones(): Int {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter : FileFilter {
            override fun accept(pathname: File): Boolean {
                //Check if filename is "cpu", followed by a single digit number
                return if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    true
                } else false
            }
        }
        return try {
            //Get directory containing CPU info
            val dir = File("/sys/devices/system/cpu/")
            //Filter to only list the devices we care about
            val files: Array<File> = dir.listFiles(CpuFilter())
            //Return the number of cores (virtual CPU devices)
            files.size
        } catch (e: Exception) {
            //Default to return 1 core
            1
        }
    }

    override fun onResume() {
        super.onResume()
        glview.onResume()
    }

    override fun onPause() {
        super.onPause()
        glview.onPause()
    }

    fun btn01(view: View) {
    }

    fun LerArquivo(FileName: String) =
        this.getAssets().open(FileName).bufferedReader().use { it.readText() }

    //TODO: Fazer chamada a partir da outra view (view manipula view)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onUserInteraction() {
        super.onUserInteraction()
        atualiza_texto()
    }

//TODO: remover métodos alocar e desalocar, estes métodos foram criados para testar se há vazamento de memória
    /** funcao de sobrecarga da memoria opencl - debug*/
    fun alocar(view: View) {
        var lista_texturas_local = emptyList<TextureWrapperImpl>().toMutableList()
        for (i in 1..128) {
            val textura = TextureWrapperImpl(64, 64)

            lista_texturas_local.add(textura)
            ////Log.i("ActivityExibirGLView","Texture ADD to list "+ i.toString() )

            //      textura.liberaRecursos()
        }
        lista_texturas.addAll(lista_texturas_local)
        ////Log.i("debug texturas ","lista tamanho  " + lista_texturas.size.toString())
        lista_texturas_local.forEach { tex ->
            resources.janela?.run {
                lock.lock()
                tarefasAlocarTextura.add(
                    TarefaAlocarTexturas(tex)
                )
                lock.unlock()
            }
        }
    }

    fun desalocar(view: View) {
        lista_texturas.forEach { textura ->
            resources.janela?.run {
                lock.lock()
                tarefasDesalocarTextura.add(
                    TarefaDesalocarTexturaGL(
                        textura
                    )
                )
                lock.unlock()
            }
        }
        lista_texturas.clear()
        ////Log.i("debug texturas ","lista tamanho  " + lista_texturas.size.toString())
    }

    fun atualiza_texto() {
        textView?.text = glview?.myRenderer?.textoDebug.toString()
    }

    //TODO: implementar, pois runOnUiThread simplesmente nao faz nada
    override fun recieveText(arg: String) {
        //estou usando onUserInteraction para atualizar o texto sob demanda


        /*   this.runOnUiThread { //não faz nada
            @Override
            fun run() {
                 ////Log.i("LISTENER ","received " + arg )
                textView?.text = arg;
            }}
    */


        /*Thread(Runnable { //dá erro de concorrencia
            ////Log.i("LISTENER ","received " + arg )
            textView?.text = arg;

        }).start()*/


    }
}
