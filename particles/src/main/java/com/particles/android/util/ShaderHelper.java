/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.particles.android.util;

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

import android.util.Log;

public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    /**
     * Loads and compiles a vertex shader, returning the OpenGL object ID.
     * 加载编译一个顶点着色器，返回对象ID
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * Loads and compiles a fragment shader, returning the OpenGL object ID.
     * 加载编译一个片段着色器，返回对象ID
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * 编译一个着色器, 返回OpenGL对象ID.
     */
    private static int compileShader(int type, String shaderCode) {
        // 创建新的着色器对象
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "无法创建新的着色器");
            }

            return 0;
        }

        // 传入着色器源码
        glShaderSource(shaderObjectId, shaderCode);

        // 编译着色器
        glCompileShader(shaderObjectId);

        // 获取编译状态
        final int[] compileStatus = new int[1];
        // 取出编译状态到数组中
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,
                compileStatus, 0);

        if (LoggerConfig.ON) {
            // Print the shader info log to the Android log output.
            Log.v(TAG, "编译源码：" + "\n" + shaderCode
                    + "\n:" + glGetShaderInfoLog(shaderObjectId));
        }

        // 验证编译状态
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId);

            if (LoggerConfig.ON) {
                Log.w(TAG, "着色器编译源码时失败");
            }

            return 0;
        }

        // Return the shader object ID.
        return shaderObjectId;
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     * 将顶点着色器和片段着色器链接到OpenGL程序中。则返回OpenGL程序对象ID，或 如果链接失败，返回0。
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // 创建新的程序对象
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Could not create new program");
            }

            return 0;
        }

        // 附上着色器到程序
        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId);
        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId);

        // 链接程序（把着色器联合起来）
        glLinkProgram(programObjectId);

        // ## 检查链接是否成功
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS,
                linkStatus, 0);

        if (LoggerConfig.ON) {
            // Print the program info log to the Android log output.
            Log.v(
                    TAG,
                    "链接程序返回值：\n"
                            + glGetProgramInfoLog(programObjectId));
        }

        // 验证链接状态
        if (linkStatus[0] == 0) {
            // 如果失败，删除程序对象
            glDeleteProgram(programObjectId);

            if (LoggerConfig.ON) {
                Log.w(TAG, "程序链接失败");
            }

            return 0;
        }

        // Return the program object ID.
        return programObjectId;
    }

    /**
     * 验证OpenGL程序
     */
    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,
                validateStatus, 0);
        Log.v(TAG, "验证程序结果: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    /**
     * Helper function that compiles the shaders, links and validates the
     * program, returning the program ID.
     */
    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        // 编译着色器
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // 链接它们到OpenGL程序
        program = linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            validateProgram(program);
        }

        return program;
    }
}
