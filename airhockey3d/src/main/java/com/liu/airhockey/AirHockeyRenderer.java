package com.liu.airhockey;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.liu.airhockey.util.MatrixHelper;
import com.liu.airhockey.util.ShaderHelper;
import com.liu.airhockey.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private int program; // 链接的程序ID

    private final Context context;

    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final int BYTES_PER_FLOAT = 4;

    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    private static final String A_COLOR = "a_Color";
    private int aColorLocation;

    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;

    // 模型矩阵移动物体
    private final float[] modelMatrix = new float[16];

    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

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
                // 坐标顺序 X, Y ,R, G, B
                // 三角形扇
                0f, 0f, 1f, 1f, 1f,

                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,

                // 线1
                -0.5f, 0f, 1f, 0f, 0f,
                0.5f, 0f, 1f, 0f, 0f,

                // 木槌
                0f, -0.4f, 0f, 0f, 1f,
                0f, 0.4f, 1f, 0f, 0f
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

        // 获得颜色属性的位置，以便稍后更新
        aColorLocation = glGetAttribLocation(program, A_COLOR);

        // 获取属性位置，告诉OpenGL到哪里找这个属性的对应数据
        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        // 获取Uniform的位置
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);

        // 关联属性与顶点数据数组
        vertexData.position(0); // 指针指向开头
        // 在缓冲区vertexData中找a_Position对应的数据，属性有默认值，(x,y,z,w)（0,0,0,1）
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        // 设置使用顶点数组
        glEnableVertexAttribArray(aPositionLocation);

        // 将顶点数据与着色器中的a_Color关联起来,属性有默认值，(R,G,B,A)（0,0,0,1）
        vertexData.position(POSITION_COMPONENT_COUNT);// 指针指向颜色数据开头的位置
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        // 设置使用颜色属性
        glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 设置视口尺寸
        glViewport(0, 0, width, height);

        // 创建一个视野度数为45°的透视投影，视椎体从z值为 [-1,-10]，
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 10f);

        // 利用模型矩阵移动物体
        Matrix.setIdentityM(modelMatrix, 0); // 设为单位矩阵
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -2.5f); // 沿z轴负方向移动2个单位
        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f); // x轴旋转-60度右手原则

        final float[] temp = new float[16];
        Matrix.multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清空屏幕
        glClear(GL_COLOR_BUFFER_BIT);

        // 传递正交投影矩阵给着色器
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        // 更新着色器代码中的 u_Color 的值,uniform与属性不同，没有默认值
        // 绘制两个三角形
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);

        // 绘制分割线
        glDrawArrays(GL_LINES, 6, 2);

        // 画两个木槌
        // 蓝色木槌
        glDrawArrays(GL_POINTS, 8, 1);
        // 红色木槌
        glDrawArrays(GL_POINTS, 9, 1);
    }
}
