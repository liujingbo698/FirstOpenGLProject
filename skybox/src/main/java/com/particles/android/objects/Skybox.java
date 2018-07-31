package com.particles.android.objects;

import android.opengl.GLES20;

import com.particles.android.data.VertexArray;
import com.particles.android.programs.SkyboxShaderProgram;

import java.nio.ByteBuffer;

import static android.opengl.GLES20.glDrawElements;

public class Skybox {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private final VertexArray vertexArray;
    private final ByteBuffer indexArray;

    public Skybox() {
        // 创建一个单元立方体
        vertexArray = new VertexArray(new float[]{
                -1, 1, 1, // 左 上 近
                1, 1, 1, // 右 上 近
                -1, -1, 1, // 左 下 近
                1, -1, 1, // 右 下 近
                -1, 1, -1, // 左 上 远
                1, 1, -1, // 右 上 远
                -1, -1, -1, // 左 下 远
                1, -1, -1  // 右 下 远
        });

        indexArray = ByteBuffer.allocateDirect(6 * 6).put(new byte[]{
                // 前
                1, 3, 0,
                0, 3, 2,
                // 后
                4, 6, 5,
                5, 6, 7,
                // 左
                0, 2, 4,
                4, 2, 6,
                // 右
                5, 7, 1,
                1, 7, 3,
                // 上
                5, 1, 4,
                4, 1, 0,
                // 下
                6, 2, 7,
                7, 2, 3
        });
        indexArray.position(0);
    }

    public void bindData(SkyboxShaderProgram shaderProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                shaderProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                0);
    }

    public void draw() {
        glDrawElements(GLES20.GL_TRIANGLES, 36, GLES20.GL_UNSIGNED_BYTE, indexArray);
    }
}
