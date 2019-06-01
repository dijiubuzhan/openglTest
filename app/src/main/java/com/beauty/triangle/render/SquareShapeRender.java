package com.beauty.triangle.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.beauty.triangle.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareShapeRender implements GLSurfaceView.Renderer {
    /*private static final String VERTEX_SHADER_FILE ="triangle_matrix_color_vertex.glsl" ;
    private static final String FRAGMENT_SHADER_FILE ="fragment.glsl" ;*/
    private static final String VERTEX_SHADER_FILE = "triangle_matrix_color_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "triangle_matrix_color_fragment_shader.glsl";
    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";

    private Context context;
    private int mProgramObjectId;

    //在数组中，一个顶点需要3个来描述其位置，需要3个偏移量
    private static final int COORDS_PER_VERTEX = 3;
    private static final int COORDS_PER_COLOR = 3;
    private FloatBuffer mVertexFloatBuffer;
    //每个Float,4个字节
    static int BYTES_PER_FLOAT = 4;



    //在数组中，描述一个顶点，总共的顶点需要的偏移量。这里因为只有位置顶点，所以和上面的值一样
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX+COORDS_PER_COLOR;
    //一个点需要的byte偏移量。
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;
    //顶点的坐标系
    private static float SQUARE_COLOR_COORDS[] = {
            //Order of coordinates: X, Y, Z, R,G,B,
            -0.5f, 0.5f, 0.0f, 1.f, 0f, 0f,  //  0.top left RED
            -0.5f, -0.5f, 0.0f, 0.f, 0f, 1f, //  1.bottom left Blue
            0.5f, 0.5f, 0.0f, 1f, 1f, 1f,   //  3.top right WHITE
            0.5f, -0.5f, 0.0f, 0.f, 1f, 0f,  //  2.bottom right GREEN
    };
    private static final int VERTEX_COUNT = SQUARE_COLOR_COORDS.length / TOTAL_COMPONENT_COUNT;

   //添加矩阵
    private static final String U_MATRIX = "u_Matrix";

    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;



    public SquareShapeRender(Context mContext) {
        context=mContext;

         /*
      1. 使用nio中的ByteBuffer来创建内存区域。
      2. ByteOrder.nativeOrder()来保证，同一个平台使用相同的顺序
      3. 然后可以通过put方法，将内存复制过去。

      因为这里是Float，所以就使用floatBuffer
       */
        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(SQUARE_COLOR_COORDS.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(SQUARE_COLOR_COORDS);
        mVertexFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,0.0f);

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
        GLES20.glUseProgram(mProgramObjectId);

        //根据我们定义的取出定义的位置
        int vPosition = GLES20.glGetAttribLocation(mProgramObjectId, A_POSITION);
        mVertexFloatBuffer.position(0);
        //3.将坐标数据放入
        GLES20.glVertexAttribPointer(
                vPosition,  //上面得到的id
                COORDS_PER_VERTEX, //告诉他用几个偏移量来描述一个顶点
                GLES20.GL_FLOAT, false,
                STRIDE, //一个顶点需要多少个字节的偏移量
                mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        //取出颜色
        int uColor = GLES20.glGetAttribLocation(mProgramObjectId, A_COLOR);
        mVertexFloatBuffer.position(COORDS_PER_VERTEX);
        GLES20.glVertexAttribPointer(
                uColor,
                COORDS_PER_COLOR,
                GLES20.GL_FLOAT, false,
                STRIDE,
                mVertexFloatBuffer);
        GLES20.glEnableVertexAttribArray(uColor);

        uMatrix = GLES20.glGetUniformLocation(mProgramObjectId, U_MATRIX);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
          GLES20.glViewport(0,0,width,height);
        //主要还是长宽进行比例缩放
        float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;

        if (width > height) {
            //横屏。需要设置的就是左右。
            Matrix.orthoM(mProjectionMatrix, 0, aspectRatio, -aspectRatio, -1, 1f, -1.f, 1f);
        } else {
            //竖屏。需要设置的就是上下
            Matrix.orthoM(mProjectionMatrix, 0, -1, 1f,aspectRatio, -aspectRatio, -1.f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        //开始绘制
        //设置绘制三角形的颜色
        GLES20.glUniformMatrix4fv(uMatrix,1,false,mProjectionMatrix,0);

        //绘制三角形.
        //draw arrays的几种方式 GL_TRIANGLES三角形
        //GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形)
        //GL_TRIANGLE_FAN扇形(可以描述圆形)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);

    }
}
