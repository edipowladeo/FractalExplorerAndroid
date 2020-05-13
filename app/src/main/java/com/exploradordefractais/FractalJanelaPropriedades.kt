package com.exploradordefractais

import java.lang.Math.pow

//TODO: Salvar em Json
open class
FractalJanelaPropriedades {
    var ExibirInformacoesDaJanelaEmOverlay = false

    val NumThreadsProcessamento = 4
    val samplingIteracoes  = 64
    var FPSalvoAoManipular = 30

    val escalaPaleta = samplingIteracoes*16f

    val limiteDivergencia = 256.0
    val maxIteracoes = 1024
    var FatorScroll = 2.0f

    var fatorEscala = 1.5f
    //samplingIteracoes*maxIteracoes < 2^16 OU SEJA TÁ INDO 16 BIT CORRETAMENTE
    var circularCores = true
//Todo: Fazer circular cores ser incremental,
    var velocidadeCircularCores = 1f

    //TODO: remover fator debug e criar janelas de alocação e inatividade
    val fator_debug = 1//0.5 //tamanho da fractalJanela de alocação, usado para debug, release deve ser =1

    val minTamanhoAparentePixel = 2 // mínimo tamanho que o pixel pode assumir na tela, valores <1 representam multisampling
    val quantidadeDeCamadasAlemDaPrincipal = 4  // camadas de preview (baixa resolucao) processadas antes da camada de alta qualidade

    var TaxaZoom = 0.0f
    var TaxaPan = 0.0f

    var MagInicial = 10
    var MaginiciallF = pow(0.5,MagInicial.toDouble())/1.12
    var PosicaoCameraInicial = CoordenadasPlanoEDelta(
        CoordenadasPlano(
            -0.5,
            0.0
        ), MaginiciallF
    )

    //var MagInicial = 15





}
