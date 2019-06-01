package com.beauty.triangle;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.beauty.triangle.render.CircleShapeRender;
import com.beauty.triangle.render.TextureRender;
import com.beauty.triangle.render.TriangleShapeRender;


public class MainActivity extends AppCompatActivity {
    GLSurfaceView glSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView=new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        //glSurfaceView.setRenderer(new TriangleShapeRender(this));
        //glSurfaceView.setRenderer(new SquareShapeRender(this));
        //glSurfaceView.setRenderer(new CircleShapeRender(this));
        glSurfaceView.setRenderer(new TextureRender(this));

        setContentView(glSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
}
