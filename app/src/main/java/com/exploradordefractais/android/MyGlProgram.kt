package com.exploradordefractais.android

import android.content.Context
import android.opengl.GLES20


class MyGLProgram(context: Context,vertexShaderName:String,fragShaderName:String) {

    private val vertexShaderCode = LerArquivo(context,vertexShaderName)
    private val fragmentShaderCode = LerArquivo(context,fragShaderName)

    private var programHandle: Int

    init {
        /** abre o código fonte GLSL e complia-os*/
        val vertexShader: Int = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader: Int = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        /** cria programa e linka os shaders*/
            programHandle = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }

        if  (programHandle == 0)
        {
            throw RuntimeException("Error creating program.");
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        /** abre o código fonte GLSL e complia-os*/
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    fun getProgramHandle(): Int {
        return programHandle
    }

}