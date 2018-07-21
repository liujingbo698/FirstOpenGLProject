package com.liu.airhockey;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.liu.airhockey.util.TextResourceReader;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private final Context context;

    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int BYTES_PER_FLOAT = 4;

    private final FloatBuffer vertexData;

    public AirHockeyRenderer(Context context) {
        this.context = context;

        float[] tableVertices = {
                0f, 0f,
                0f, 14f,
                9f, 14f,
                9f, 0f
        };

        float[] tableVerticesWithTriangles = {
                // 三角形1
                0f, 0f,
                9f, 14f,
                0f, 14f,

                // 三角形2
                0f, 0f,
                9f, 0f,
                9f, 14f,

                // 线1
                0f, 7f,
                9f, 7f,

                // 木槌
                4.5f, 2f,
                4.5f, 12f
        };

        vertexData = ByteBuffer
                // 直接分配
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                // 本地字节排序
                .order(ByteOrder.nativeOrder())
                // 作为FloatBuffer输出
                .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 设置清空屏幕颜色
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        // 读取顶点着色器代码
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);

        // 读取片段着色器代码
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置视口尺寸
        glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清空屏幕
        glClear(GL_COLOR_BUFFER_BIT);

    }
}
