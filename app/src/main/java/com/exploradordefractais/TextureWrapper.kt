package com.exploradordefractais

import java.nio.ByteBuffer

interface TextureWrapper {


//Todo: Alterar implementação e deixar interface sómente com métodos abaixo:
    fun bind()
    fun fillTextureFromBuffer(buffer: ByteBuffer)
    fun desalocarTexturaGL()
    fun alocarTexturaGL()
}