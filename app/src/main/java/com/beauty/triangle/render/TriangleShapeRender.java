package com.beauty.triangle.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;


import com.beauty.triangle.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleShapeRender implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER_FILE ="vertes.glsl" ;
    private static final String FRAGMENT_SHADER_FILE ="fragment.glsl" ;

    private Context context;
    private int mProgramObjectId;

    //在数组中，一个顶点需要3个来描述其位置，需要3个偏移量
    private static final int COORDS_PER_VERTEX = 3;
    private static final int COORDS_PER_COLOR = 0;
    private FloatBuffer mVertexFloatBuffer;
    //每个Float,4个字节
    static int BYTES_PER_FLOAT = 4;


    // 设置图形的RGB值和透明度
    float TRIANGLE_COLOR[] = {1f, 0f, 0f, 1.0f};

    //在数组中，描述一个顶点，总共的顶点需要的偏移量。这里因为只有位置顶点，所以和上面的值一样
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX+COORDS_PER_COLOR;
    //一个点需要的byte偏移量。
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;
    //顶点的坐标系
    private static float TRIANGLE_COORDS[] = {
            //Order of coordinates: X, Y, Z
            0.5f, 0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f   // bottom right
    };
    private static final int VERTEX_COUNT = TRIANGLE_COORDS.length / TOTAL_COMPONENT_COUNT;




    public TriangleShapeRender(Context mContext) {
        context=mContext;

         /*
      1. 使用nio中的ByteBuffer来创建内存区域。
      2. ByteOrder.nativeOrder()来保证，同一个平台使用相同的顺序
      3. 然后可以通过put方法，将内存复制过去。

      因为这里是Float，所以就使用floatBuffer
       */
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(TRIANGLE_COORDS.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TRIANGLE_COORDS);
        //因为是从第一个点开始，就表示三角形的，所以将position移动到0
        mVertexFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f,1f,0f,1f);//设置底色为绿色，要与GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)配合使用

        //0.先从Asset中得到着色器的代码
        String vertexShaderCode = GLESUtils.readAssetShaderCode(context, VERTEX_SHADER_FILE);
        String fragmentShaderCode = GLESUtils.readAssetShaderCode(context, FRAGMENT_SHADER_FILE);

        //1.得到之后，进行编译。得到id
        int vertexShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShaderObjectId = GLESUtils.compileShaderCode(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //3.继续套路。取得到program
        mProgramObjectId = GLES20.glCreateProgram();
        //将shaderId绑定到program当中
        GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
        GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
        //4.最后，启动GL link program
        GLES20.glLinkProgram(mProgramObjectId);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0,0,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
       GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //0.先使用这个program?这一步应该可以放到onCreate中进行
        GLES20.glUseProgram(mProgramObjectId);

        //1.根据我们定义的取出定义的位置
        int vPosition = GLES20.glGetAttribLocation(mProgramObjectId, "aPosition");
        //2.开始启用我们的position
        GLES20.glEnableVertexAttribArray(vPosition);
        //3.将坐标数据放入
        GLES20.glVertexAttribPointer(
                vPosition,  //上面得到的id
                COORDS_PER_VERTEX, //告诉他用几个偏移量来描述一个顶点
                GLES20.GL_FLOAT, false,
                STRIDE, //一个顶点需要多少个字节的偏移量
                mVertexFloatBuffer);

        //取出颜色
        int uColor = GLES20.glGetUniformLocation(mProgramObjectId, "uColor");

        //开始绘制
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(
                uColor,
                1,
                TRIANGLE_COLOR,
                0
        );

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形
        //GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形)
        //GL_TRIANGLE_FAN扇形(可以描述圆形)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(vPosition);
    }

}
