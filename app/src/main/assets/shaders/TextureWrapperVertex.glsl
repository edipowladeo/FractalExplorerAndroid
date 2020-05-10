//#version 120

precision mediump float;

uniform vec2 u_WindowSize;

uniform vec3 u_SpritePosition; //vec3(X,Y,Escala)

attribute vec2 a_RectangleVertices;
varying vec2 v_TexturePosition;


void main()
{
    vec2 posicao_tela = (a_RectangleVertices.xy*u_SpritePosition.z+u_SpritePosition.xy);
    gl_Position = vec4(posicao_tela/u_WindowSize.xy,1.0,1.0);
    v_TexturePosition = vec2(a_RectangleVertices.x,-a_RectangleVertices.y);
}