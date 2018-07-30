package com.liu.airhockey.programs;

import android.content.Context;

import com.liu.airhockey.util.ShaderHelper;
import com.liu.airhockey.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

public class ShaderProgram {
    // Uniform 常量
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_COLOR = "u_Color";

    // Attribute 常量
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    // 着色器程序
    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        // 编译着色器和链接到程序
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));
    }

    public void useProgram() {
        glUseProgram(program);
    }

}
