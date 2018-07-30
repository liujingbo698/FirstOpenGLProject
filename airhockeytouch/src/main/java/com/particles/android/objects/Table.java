/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.particles.android.objects;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static com.airhockey.android.Constants.BYTES_PER_FLOAT;

import com.particles.android.data.VertexArray;
import com.particles.android.programs.TextureShaderProgram;

public class Table {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT
            + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private static final float[] VERTEX_DATA = {
            // 坐标顺序 X，Y，S，T

            // 三角形扇面
            0.0f,  0.0f, 0.5f, 0.5f,
            -0.5f, -0.8f, 0.0f, 0.9f,
            0.5f, -0.8f, 1.0f, 0.9f,
            0.5f,  0.8f, 1.0f, 0.1f,
            -0.5f,  0.8f, 0.0f, 0.1f,
            -0.5f, -0.8f, 0.0f, 0.9f
            /* T分量正是按照Y分量相反方向定义 */
            /*
             * 桌子宽1个单位，高1.6个单位，纹理像素 512 ✖ 1024，纹理对应宽为1个单位，高为2个单位。
             * 所以：纹理范围使用 0.1-0.9，实际就对应0.2-1.8的单位，总共1.6个单位，取纹理像素T轴中间部分
             * 若不剪裁，纹理将压扁
             * 若不剪裁，坚持使用0.0到1.0纹理坐标，把纹理预拉伸，这样压扁后就看上去正确了，用这种方法，上面那些
             * 无法显示的纹理部分就不会占用任何内存了
             */
    };

    private final VertexArray vertexArray;

    public Table() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE);

        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }
}
