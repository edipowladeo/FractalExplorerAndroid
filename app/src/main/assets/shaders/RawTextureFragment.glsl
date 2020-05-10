//#version 120

#if __VERSION__ < 130
#define TEXTURE2D texture2D
#else
#define TEXTURE2D texture
#endif

precision highp float;

uniform sampler2D u_Texture;    // The input texture.
uniform float u_tempo;
uniform float u_escalaPaleta;

varying vec2 v_TexturePosition;		// Interpolated position for this fragment.
varying vec4 v_Color;

vec4 paleta(int iteracoes,float deltaT){
    vec4 cor;
    cor.a = 1.0;
    if (iteracoes == 0) return cor;
    float iteracoesFloat = float(iteracoes);
 iteracoesFloat/=u_escalaPaleta;
 //   iteracoesFloat *= 6.2/256.0;
    iteracoesFloat+=deltaT;

    cor.r = 0.5+0.5*sin(iteracoesFloat);
    cor.g = 0.5+0.5*sin(iteracoesFloat+2.0);
    cor.b = 0.5+0.5*sin(iteracoesFloat-2.0);

    return cor;
}

int getI(float valueF){
    return int(valueF*256.0);
}

void main()
{
    vec4 texcolor = TEXTURE2D(u_Texture, v_TexturePosition.xy);
    vec4 outputcolor = vec4(0.0,0.0,0.0,1.0);

    int valor_inteiro = getI(texcolor.r);
    valor_inteiro += getI(texcolor.g)*256;
    valor_inteiro += getI(texcolor.b)*65536;
    valor_inteiro += getI(texcolor.a)*16777216;

    outputcolor = paleta(valor_inteiro,u_tempo);
    gl_FragColor = outputcolor;


//gl_FragColor = color;
}
