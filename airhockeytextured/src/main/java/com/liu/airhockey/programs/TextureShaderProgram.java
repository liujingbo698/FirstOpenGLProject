package com.liu.airhockey.programs;

import android.content.Context;

import com.liu.airhockey.R;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class TextureShaderProgram extends ShaderProgram {
    // Uniform 位置
    private final int uMatrixLocation;
    private final int uTextureUnitLocation;

    // Attribute 位置
    private final int aPositionLocation;
    private final int aTextureCoordinatesLocation;

    public TextureShaderProgram(Context context) {
        super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);

        // 从 着色器程序 检索uniform位置
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        // 从 着色器程序 检索attribute位置
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void setUniforms(float[] matrix, int textureId) {
        // 传递矩阵到着色器程序
        glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // 把活动的纹理单元设为 纹理单元0
        glActiveTexture(GL_TEXTURE0);

        // 绑定纹理到这个纹理单元
        glBindTexture(GL_TEXTURE_2D, textureId);

        // 把选定的纹理单元（TEXTURE0）传递给片段着色器中的 u_TextureUnit
        glUniform1i(uTextureUnitLocation, 0);
    }

    public int getPositionAttributeLocation(){
        return aPositionLocation;
    }

    public int getTextureCoordinatesAttributeLocation(){
        return aTextureCoordinatesLocation;
    }
}
