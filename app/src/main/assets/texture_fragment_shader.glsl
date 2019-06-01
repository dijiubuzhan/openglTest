precision mediump float;

//在片元着色器这里添加这个 sampler2D 表示我们要添加2D贴图
uniform sampler2D u_TextureUnit;
//定义一个u_ChangeColor,因为颜色的变量是RGB,所以使用vec3
uniform vec3 u_ChangeColor;

varying vec2 v_TextureCoordinates;

void main(){
    //得到2d color
    vec4 nColor=texture2D(u_TextureUnit,v_TextureCoordinates);
   //黑白图片
    float c= nColor.r*u_ChangeColor.r+nColor.g*u_ChangeColor.g+nColor.b*u_ChangeColor.b;
    gl_FragColor = vec4(c,c,c,nColor.a);
}