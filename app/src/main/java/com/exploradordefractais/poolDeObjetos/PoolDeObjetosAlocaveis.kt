package com.exploradordefractais.poolDeObjetos

import com.exploradordefractais.Alocavel
import com.exploradordefractais.SegundosI
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//TODO: tirar alocavel do template, criar para T genérico

class PoolDeObjetosAlocaveis<T: Alocavel>(val construtorDoObjeto:()->T) {
    var pool: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue()
    private var servicoExecutorAssincrono: ScheduledExecutorService? = null

    /**
     * Creates the pool.
     *
     * @param qtdeMinAlocar minimum number of objects residing in the pool
     */
    constructor(construtorDoObjeto:()->T, qtdeMinAlocar: Int):this(construtorDoObjeto) {
        alocarPool(qtdeMinAlocar)
    }

    /**
     * Creates the pool.
     *
     * @param qtdeMinAlocar            minimum number of objects residing in the pool
     * @param qtdeMaxAlocar            maximum number of objects residing in the pool
     * @param validationInterval time in seconds for periodical checking of minIdle / maxIdle conditions in a separate thread.
     * When the number of objects is less than minIdle, missing instances will be created.
     * When the number of objects is greater than maxIdle, too many instances will be removed.
     */
    constructor(construtorDoObjeto: () -> T,
                qtdeMinAlocar: Int, qtdeMaxAlocar: Int, validationInterval: SegundosI
    )
            :this(construtorDoObjeto)
    {
        alocarPool(qtdeMinAlocar)
        // check pool conditions in a separate thread
        servicoExecutorAssincrono = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleWithFixedDelay(Runnable {
                val size: Int = pool.size
                if (size < qtdeMinAlocar) {
                    val sizeToBeAdded = qtdeMinAlocar - size
                    for (i in 0 until sizeToBeAdded) {
                        pool.add(construtorDoObjeto())
                    }
                } else if (size > qtdeMaxAlocar) {
                    val sizeToBeRemoved = size - qtdeMaxAlocar
             //       Log.i("PoolDeObjetosAlocaveis", "Removendo ${sizeToBeRemoved} itens")
                    for (i in 0 until sizeToBeRemoved) {
                        pool.poll()?.desalocar()
                    }
                }
            //    Log.i("PoolDeObjetosAlocaveis", "Tam desejado: ${qtdeMinAlocar}~${qtdeMaxAlocar}, itens: ${pool.size}")
            }, validationInterval, validationInterval, TimeUnit.SECONDS)
        }
    }

    /**
     * Gets the next free object from the pool. If the pool doesn't contain any objects,
     * a new object will be created and given to the caller of this method back.
     *
     * @return T borrowed object
     */
    fun emprestaObjeto(): T {
        var objeto = pool.poll()
        if (objeto == null) {
            return construtorDoObjeto()
        }
        return objeto
    }

    fun devolveObjeto(objeto: T) {
        pool.add(objeto)
    }

    /**
     * Shutdown this pool.
     */

    //TODO: implementar
    fun shutdown() {
        servicoExecutorAssincrono?.let { shutdown() }

    }

    private fun alocarPool(qtdeObjetos: Int) {
        pool = ConcurrentLinkedQueue<T>()
        for (i in 0 until qtdeObjetos) {
            pool.add(construtorDoObjeto())
        }
    }

    fun getSize():Int{
        return pool.size
    }
}