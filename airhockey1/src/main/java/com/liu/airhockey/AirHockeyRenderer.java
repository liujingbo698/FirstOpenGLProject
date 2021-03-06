package com.liu.airhockey;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.liu.airhockey.util.ShaderHelper;
import com.liu.airhockey.util.TextResourceReader;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private int program; // 链接的程序ID

    private final Context context;

    private static final String U_COLOR = "u_Color";
    private int uColorPosition;

    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

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
                -0.5f, -0.5f,
                0.5f, 0.5f,
                -0.5f, 0.5f,

                // 三角形2
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f, 0.5f,

                // 线1
                -0.5f, 0f,
                0.5f, 0f,

                // 木槌
                0f, -0.25f,
                0f, 0.25f
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
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // 读取顶点着色器代码
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);

        // 读取片段着色器代码
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        // 链接两个着色器为一个程序
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        // 使用这个程序
        glUseProgram(program);

        // 获得uniform的位置，以便稍后更新
        uColorPosition = glGetUniformLocation(program, U_COLOR);

        // 获取属性位置，告诉OpenGL到哪里找这个属性的对应数据
        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        // 关联属性与顶点数据数组
        vertexData.position(0); // 指针指向开头
        // 在缓冲区vertexData中找a_Position对应的数据，属性有默认值，(x,y,z,w)（0,0,0,1）
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);

        // 设置使用顶点数组
        glEnableVertexAttribArray(aPositionLocation);
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

        // 更新着色器代码中的 u_Color 的值,uniform与属性不同，没有默认值
        glUniform4f(uColorPosition, 1.0f, 1.0f, 1.0f, 1.0f);
        // 绘制两个三角形
        glDrawArrays(GL_TRIANGLES, 0, 6);

        // 绘制分割线
        glUniform4f(uColorPosition, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 6, 2);

        // 画两个木槌
        // 蓝色木槌
        glUniform4f(uColorPosition,0.0f,0.0f,1.0f,1.0f);
        glDrawArrays(GL_POINTS, 8, 1);
        // 红色木槌
        glUniform4f(uColorPosition,1.0f,0.0f,0.0f,1.0f);
        glDrawArrays(GL_POINTS, 9, 1);
    }
}
