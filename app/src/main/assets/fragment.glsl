//设置片元着色器的精度。这里值要兼容性能和效率。通常都是选择mediump
precision mediump float;

//定义个常量 uColor
uniform vec4 uColor;
void main(){
//同样。这里的gl_FragColor是内置的变量
gl_FragColor = uColor;
}