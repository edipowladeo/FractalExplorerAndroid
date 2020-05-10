package com.exploradordefractais.android

import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log
import com.exploradordefractais.Alocavel
import com.exploradordefractais.TextureWrapper
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MyTextureWrapper(val largura:Int, val altura:Int): TextureWrapper , Alocavel{

    //TODO: empty buffer deve ser passado como parametro dinamico, ou sequer utilizado
    companion object {
        var totalObjetosAlocados = 0
    }

    private var textureHandle = IntArray(1) { 0 }
    private var flagAlocouTexturaGLSucesso = false

   override fun alocadoComSucesso():Boolean{
        return flagAlocouTexturaGLSucesso
    }

    override fun fillTextureFromBuffer(buffer: ByteBuffer) {
     //   Log.i("TextureWrapper ", "Populate,  handle = ${textureHandle[0]}, total: ${totalObjetosAlocados}")
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
         GLES20.glTexSubImage2D(
            GLES20.GL_TEXTURE_2D, 0, 0, 0,
            largura, altura, GLES20.GL_RGBA,  GLES20.GL_UNSIGNED_BYTE, buffer
        )
    }

    override fun bind() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
    }

    override fun desalocarTexturaGL() {
        if (textureHandle[0]==0) throw RuntimeException("Tentando desalocar Textura sem handle TextureWrapper.desalocarTexturaGL()")
        totalObjetosAlocados--
      //  Log.i("TextureWrapper ", "Destroyed,  handle = ${textureHandle[0]}, total: ${totalObjetosAlocados}")
        GLES20.glDeleteTextures(1, textureHandle, 0)
    }

    override fun alocarTexturaGL() {

        GLES20.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0) {
            flagAlocouTexturaGLSucesso = true;
            totalObjetosAlocados++
          //  Log.i("TextureWrapper ", "Created, handle = ${textureHandle[0]}, total: ${totalObjetosAlocados}")
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
            // Set filtering
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )

            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                largura, altura, 0, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
            )
        } else {
            throw RuntimeException("Erro ao Criar Textura. glGenTextures()")
        }


    }
    override fun desalocar() {
       desalocarTexturaGL()
    }

    /** APAGAR DA INTERFACE*/

    override fun alocar() {
        TODO("Not yet implemented")
    }
}
