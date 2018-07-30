/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.particles.android.util;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class TextureHelper {
    private static final String TAG = "TextureHelper";

    /**
     * Loads a texture from a resource ID, returning the OpenGL ID for that
     * texture. Returns 0 if the load failed.
     * 从一个资源ID载入一个纹理，返回纹理的OpenGL ID，失败返回0
     *
     * @param context
     * @param resourceId
     * @return
     */
    public static int loadTexture(Context context, int resourceId) {
        // 用来接收纹理ID的数组
        final int[] textureObjectIds = new int[1];
        // 生成新的纹理对象
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "不能生成一个新的OpenGL纹理对象.");
            }

            return 0;
        }
        // 读入图像文件数据，并解码，解压缩为OpenGL能理解的形式
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // 要原始图像数据，并非缩放版本
        options.inScaled = false;

        // 读取资源
        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if (bitmap == null) {
            if (LoggerConfig.ON) {
                Log.w(TAG, "Resource ID " + resourceId
                        + " could not be decoded.");
            }

            glDeleteTextures(1, textureObjectIds, 0);

            return 0;
        }

        // 绑定这个纹理到OpenGL
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        // 设置过滤：必须设置默认值，否则纹理将被设置为黑色。
        glTexParameteri(GL_TEXTURE_2D,
                GL_TEXTURE_MIN_FILTER,
                GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,
                GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // 载入Bitmap到纹理，并加入边框
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        // Note: 以下代码可能会导致错误报告在ADB日志中
        // E/IMGSRV(20095): :0: HardwareMipGen:Failed to generate texture mipmap levels (error=3)
        // No OpenGL error will be encountered (glGetError() will return 0).
        // 如果发生这种情况，只需将源图像压扁成正方形即可。因为它看起来将和纹理坐标是一样的。
        // 而且MIPMAP生成器将工作。

        glGenerateMipmap(GL_TEXTURE_2D);

        // Recycle the bitmap, since its data has been loaded into
        // OpenGL.
        // 回收位图
        bitmap.recycle();

        // Unbind from the texture.
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }
}
