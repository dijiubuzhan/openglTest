package com.beauty.triangle;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.beauty.triangle.render.CircleShapeRender;
import com.beauty.triangle.render.CustomTextureRender;
import com.beauty.triangle.render.TextureRender;
import com.beauty.triangle.render.TriangleShapeRender;


public class MainActivity extends AppCompatActivity {
    GLSurfaceView glSurfaceView;
    MyGLSurfaceView myGLSurfaceView;
    boolean isDebug=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView=new GLSurfaceView(this);
        myGLSurfaceView=new MyGLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        myGLSurfaceView.setEGLContextClientVersion(2);
        //glSurfaceView.setRenderer(new TriangleShapeRender(this));
        //glSurfaceView.setRenderer(new SquareShapeRender(this));
        glSurfaceView.setRenderer(new CircleShapeRender(this));
      //  glSurfaceView.setRenderer(new TextureRender(this));
        myGLSurfaceView.setRenderer(new CustomTextureRender(this));

        if (isDebug) {
            setContentView(myGLSurfaceView);
        }else {
            setContentView(glSurfaceView);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isDebug) {
            myGLSurfaceView.onPause();
        }else {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDebug) {
            myGLSurfaceView.onResume();
        }else {
            glSurfaceView.onResume();
        }

    }
}
