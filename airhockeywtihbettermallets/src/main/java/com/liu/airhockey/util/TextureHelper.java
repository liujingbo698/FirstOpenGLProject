package com.liu.airhockey.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

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

public class TextureHelper {
    private static final String TAG = "TextureHelper";

    public static int loadTexture(Context context, int resourceId) {
        final int[] textureObjectIds = new int[1];
        // 生成新的纹理对象
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            LogUtil.w(TAG, "不能生成一个新的OpenGL纹理对象。");
            return 0;
        }

        // 读入图像文件数据，并解码，解压缩为OpenGL能理解的形式
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // 要原始图像数据，并非缩放版本

        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (bitmap == null) {
            LogUtil.w(TAG,"资源ID "+resourceId+ " 不能被解码");
            // 图像解码失败，删除已经创建的纹理对象
            glDeleteTextures(1, textureObjectIds, 0);
            return 0;
        }

        // 告诉OpenGL后面纹理调用应用于这个纹理对象，即为绑定
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]); // 作为二位纹理对待

        // 设置缩小情况过滤器，三线性过滤（MIP贴图级别之间插值的双线性过滤）
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_LINEAR);
        // 设置放大情况过滤器，双线性过滤
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // 加载位图数据到OpenGL中，并复制到当前绑定的纹理对象
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();// 数据被复制后就可以释放这个位图了

        // 生成MIP贴图
        glGenerateMipmap(GL_TEXTURE_2D);

        // 解绑这个纹理，防止意外调用改变这个纹理
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }
}
