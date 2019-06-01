package com.beauty.triangle.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.beauty.triangle.GLESUtils;
import com.beauty.triangle.bean.Circle;
import com.beauty.triangle.bean.Point;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CircleShapeRender implements GLSurfaceView.Renderer {
    /*private static final String VERTEX_SHADER_FILE ="triangle_matrix_color_vertex.glsl" ;
private static final String FRAGMENT_SHADER_FILE ="fragment.glsl" ;*/
    private static final String VERTEX_SHADER_FILE = "triangle_matrix_color_vertex_shader.glsl";
    private static final String FRAGMENT_SHADER_FILE = "triangle_matrix_color_fragment_shader.glsl";
    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";
    private static  int VERTEX_COUNT ;

    private Context context;
    private int mProgramObjectId;

    //在数组中，一个顶点需要3个来描述其位置，需要3个偏移量
    private static final int COORDS_PER_VERTEX = 3;
    private static final int COORDS_PER_COLOR = 3;
    private FloatBuffer mVertexFloatBuffer;
    //每个Float,4个字节
    static int BYTES_PER_FLOAT = 4;
    private final float[] mCircleColorCoords;



    //在数组中，描述一个顶点，总共的顶点需要的偏移量。这里因为只有位置顶点，所以和上面的值一样
    private static final int TOTAL_COMPONENT_COUNT = COORDS_PER_VERTEX+COORDS_PER_COLOR;
    //一个点需要的byte偏移量。
    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;



    //添加矩阵
    private static final String U_MATRIX = "u_Matrix";

    private float[] mProjectionMatrix = new float[16];
    private int uMatrix;



    public CircleShapeRender(Context mContext) {
        context=mContext;
        //创建一个圆。
        //圆心
        Point center = new Point(0f, 0f, 0f);
        //圆的半径
        float radius = 0.5f;
        //多少个点来切分这个圆.越多的切分。越圆
        int numbersRoundCircle = 360;
        //
        Circle circle = new Circle(center, radius);
        mCircleColorCoords=createCircleCoords(circle,numbersRoundCircle);

        mVertexFloatBuffer = ByteBuffer
                .allocateDirect(mCircleColorCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mCircleColorCoords);
        mVertexFloatBuffer.position(0);

        VERTEX_COUNT=getCircleVertexNum(getCircleVertexNum(numbersRoundCircle));
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

    private float[] createCircleCoords(Circle circle, int numbersRoundCircle) {
        //先计算总共需要多少个点
        int needNumber = getCircleVertexNum(numbersRoundCircle);
        //创建数组
        float[] circleColorCoord = new float[needNumber * TOTAL_COMPONENT_COUNT];
        //接下来给每个点分配数据

        //对每一组点进行赋值
        for (int numberIndex = 0; numberIndex < needNumber; numberIndex++) {
            int indexOffset = numberIndex * TOTAL_COMPONENT_COUNT;

            if (numberIndex == 0) {   //第一个点。就是圆心
                //位置
                circleColorCoord[indexOffset] = circle.center.x;
                circleColorCoord[indexOffset + 1] = circle.center.y;
                circleColorCoord[indexOffset + 2] = circle.center.z;

                //下面是颜色。给一个白色
                circleColorCoord[indexOffset + 3] = 1.f;
                circleColorCoord[indexOffset + 4] = 1.f;
                circleColorCoord[indexOffset + 5] = 1.f;
            } else if (numberIndex < needNumber ) {    //切分圆的点
                //需要根据半径。中心点。来结算
                int angleIndex = numberIndex - 1;
                float angleRadius = (float) (((float) angleIndex / (float) numbersRoundCircle) * Math.PI * 2f);
                float centerX = circle.center.x;
                float centerY = circle.center.y;
                float centerZ = circle.center.z;
                float radius = circle.radius;
                float tempX = (float) (centerX + radius * Math.cos(angleRadius));
                float tempY = (float) (centerY + radius * Math.sin(angleRadius));
                float temp = centerZ + 0;

                //位置

                circleColorCoord[indexOffset] = tempX;
                circleColorCoord[indexOffset + 1] = tempY;
                circleColorCoord[indexOffset + 2] = temp;

                //下面是颜色。根据角度计算颜色
                circleColorCoord[indexOffset + 3] = (float) (1.f* Math.cos(angleRadius));
                circleColorCoord[indexOffset + 4] = (float) (1.f* Math.sin(angleRadius));
                circleColorCoord[indexOffset + 5] = 0.5f;
            }

        }
        return circleColorCoord;
    }

    /*
需要的点的个数等于 1(圆心)+切分圆的点数+1(为了闭合，切分圆的起点和终点，需要重复一次)
 */
    private int getCircleVertexNum(int numbersRoundCircle) {
        return +1 + numbersRoundCircle + 1;
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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, VERTEX_COUNT);

    }
}
