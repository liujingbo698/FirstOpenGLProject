package com.liu.airhockey.util;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        // 创建一个新的着色器对象，并检查是否创建成功
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            LogUtil.w(TAG, "无法创建新的着色器");
            return 0;
        }

        // 上传着色器源码到着色器对象中
        glShaderSource(shaderObjectId, shaderCode);
        // 编译这个着色器
        glCompileShader(shaderObjectId);

        // 检查是否成功的编译这个着色器
        final int[] compileStatus = new int[1];
        // 取出编译状态到数组中
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        LogUtil.v(TAG, "编译源码：\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId));

        // 检查是否编译成功
        if (compileStatus[0] == 0) {
            // 编译失败，删除着色器对象
            glDeleteShader(shaderObjectId);

            LogUtil.w(TAG, "着色器编译源码时失败");

            return 0;
        }

        return shaderObjectId;
    }

    /**
     * OpenGL程序就是：把一个顶点着色器和一个片段着色器链接在一变成单个对象
     *
     * @param vertexShaderId
     * @param fragmentShaderId
     * @return
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // 新建OpenGL程序
        final int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            LogUtil.w(TAG, "无法创建新程序");
            return 0;
        }

        // 附上着色器
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        // 链接程序（把着色器联合起来）
        glLinkProgram(programObjectId);

        // 检查链接是否成功
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);
        LogUtil.v(TAG, "链接程序返回值：\n" + glGetProgramInfoLog(programObjectId));

        // 验证链接状态
        if (linkStatus[0] == 0) {
            // 如果失败，删除程序对象
            glDeleteProgram(programObjectId);
            LogUtil.w(TAG,"程序链接失败");
            return 0;
        }
        return programObjectId;
    }

    /**
     * 验证OpenGL程序
     * @param programObjectId
     * @return
     */
    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);

        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        LogUtil.v(TAG, "验证程序结果：\n" + validateStatus[0] + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;

        // 编译着色器
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // 链接着色器到程序
        program = linkProgram(vertexShader, fragmentShader);

        // 验证程序
        if (LogUtil.ON) {
            validateProgram(program);
        }

        return program;
    }
}
