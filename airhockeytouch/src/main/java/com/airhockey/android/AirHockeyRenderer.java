/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package com.airhockey.android;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.invertM;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;

import com.particles.android.objects.Mallet;
import com.particles.android.objects.Puck;
import com.particles.android.objects.Table;
import com.particles.android.programs.ColorShaderProgram;
import com.particles.android.programs.TextureShaderProgram;
import com.particles.android.util.Geometry;
import com.particles.android.util.Geometry.Plane;
import com.particles.android.util.Geometry.Point;
import com.particles.android.util.Geometry.Ray;
import com.particles.android.util.Geometry.Sphere;
import com.particles.android.util.Geometry.Vector;
import com.particles.android.util.MatrixHelper;
import com.particles.android.util.TextureHelper;

public class AirHockeyRenderer implements Renderer {
    private final Context context;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] invertedViewProjectionMatrix = new float[16];// 反转矩阵：取消视图矩阵和投影矩阵的效果
    private final float[] modelViewProjectionMatrix = new float[16];

    private Table table;
    private Mallet mallet;
    private Puck puck;

    private TextureShaderProgram textureProgram;
    private ColorShaderProgram colorProgram;

    private int texture;

    private boolean malletPressed = false;
    private Point blueMalletPosition;

    // 木槌的边界
    private final float leftBound = -0.5f;
    private final float rightBound = 0.5f;
    private final float farBound = -0.8f;
    private final float nearBound = 0.8f;

    // 早先木槌的位置
    private Point previousBlueMalletPosition;

    // 存储冰球的位置和方向
    private Point puckPosition;
    private Vector puckVector;

    public AirHockeyRenderer(Context context) {
        this.context = context;
    }

    public void handleTouchPress(float normalizedX, float normalizedY) {

        Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);

        // Now test if this ray intersects with the mallet by creating a
        // bounding sphere that wraps the mallet.
        Sphere malletBoundingSphere = new Sphere(new Point(
                blueMalletPosition.x,
                blueMalletPosition.y,
                blueMalletPosition.z),
                mallet.height / 2f);

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the mallet's bounding sphere), then set malletPressed =
        // true.
        malletPressed = Geometry.intersects(malletBoundingSphere, ray);
    }

    private Ray convertNormalized2DPointToRay(
            float normalizedX, float normalizedY) {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
        final float[] farPointNdc = {normalizedX, normalizedY, 1, 1};

        final float[] nearPointWorld = new float[4];
        final float[] farPointWorld = new float[4];

        multiplyMV(
                nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
        multiplyMV(
                farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld);
        divideByW(farPointWorld);

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        Point nearPointRay =
                new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);

        Point farPointRay =
                new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

        return new Ray(nearPointRay,
                Geometry.vectorBetween(nearPointRay, farPointRay));
    }

    private void divideByW(float[] vector) {
        vector[0] /= vector[3];
        vector[1] /= vector[3];
        vector[2] /= vector[3];
    }


    public void handleTouchDrag(float normalizedX, float normalizedY) {

        if (malletPressed) {
            Ray ray = convertNormalized2DPointToRay(normalizedX, normalizedY);
            // 定义一个代表我们的空气曲棍球桌的平面。
            Plane plane = new Plane(new Point(0, 0, 0), new Vector(0, 1, 0));
            // 找出触摸点与代表我们的桌子的平面相交的位置。我们将把木槌沿这个平面移动。
            Point touchedPoint = Geometry.intersectionPoint(ray, plane);
            // 夹紧边界

            // 存储先前木槌的点位置
            previousBlueMalletPosition = blueMalletPosition;            
            /*
            blueMalletPosition =
                new Point(touchedPoint.x, mallet.height / 2f, touchedPoint.z);
            */
            // Clamp to bounds            
            blueMalletPosition = new Point(
                    // 现在X坐标，在边界内
                    clamp(touchedPoint.x,
                            leftBound + mallet.radius,
                            rightBound - mallet.radius),
                    mallet.height / 2f,
                    // 限制Z坐标
                    clamp(touchedPoint.z,
                            0f + mallet.radius,
                            nearBound - mallet.radius));

            // 现在测试，木槌是否击打了冰球，计算两个点的距离
            float distance =
                    Geometry.vectorBetween(blueMalletPosition, puckPosition).length();

            // 距离小于两个半径之和，就表示被击打了
            if (distance < (puck.radius + mallet.radius)) {
                // 冰球被击打. 现在基于木槌的速度，让冰球飞出
                // 我们用前一个木槌的位置和当前木槌的位置给冰球创建一个方向向量，木槌移动的越快，那向量就会越大
                puckVector = Geometry.vectorBetween(
                        previousBlueMalletPosition, blueMalletPosition);
            }
        }
    }

    private float clamp(float value, float min, float max) {
        // 如果 value 小于最小值，取最小值，大于最大值，取最大值
        return Math.min(max, Math.max(value, min));
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        table = new Table();
        mallet = new Mallet(0.08f, 0.15f, 32);
        puck = new Puck(0.06f, 0.02f, 32);

        // 存储木槌的位置
        blueMalletPosition = new Point(0f, mallet.height / 2f, 0.4f);
        // 初始化冰球的位置和方向
        puckPosition = new Point(0f, puck.height / 2f, 0f);
        puckVector = new Vector(0f, 0f, 0f);

        textureProgram = new TextureShaderProgram(context);
        colorProgram = new ColorShaderProgram(context);

        texture = TextureHelper.loadTexture(context, R.drawable.air_hockey_surface);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        // Set the OpenGL viewport to fill the entire surface.
        // 设置视口尺寸
        glViewport(0, 0, width, height);

        // 创建一个视野度数为45°的透视投影，视椎体从z值为 [-1,-10]，
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width
                / (float) height, 1f, 10f);

        setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        // 清理 the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // 根据冰球向量改变冰球位置
        puckPosition = puckPosition.translate(puckVector);

        // 为冰球增加边界检查，碰到边缘，就将冰球弹开
        if (puckPosition.x < leftBound + puck.radius
                || puckPosition.x > rightBound - puck.radius) {
            // 碰到左右边缘，反转x值
            puckVector = new Vector(-puckVector.x, puckVector.y, puckVector.z);
            // 反弹加入额外的阻尼
            puckVector = puckVector.scale(0.9f);
        }
        if (puckPosition.z < farBound + puck.radius
                || puckPosition.z > nearBound - puck.radius) {
            // 碰到上下边缘，反转z值
            puckVector = new Vector(puckVector.x, puckVector.y, -puckVector.z);
            puckVector = puckVector.scale(0.9f);
        }
        // 冰球新的位置，在边缘内
        puckPosition = new Point(
                clamp(puckPosition.x, leftBound + puck.radius, rightBound - puck.radius),
                puckPosition.y,
                clamp(puckPosition.z, farBound + puck.radius, nearBound - puck.radius)
        );

        // Friction factor
        // 增加摩擦
        puckVector = puckVector.scale(0.99f);

        // Update the viewProjection matrix, and create an inverted matrix for
        // touch picking.
        // 更新投影视图矩阵，并创建一个反转矩阵
        multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0,
                viewMatrix, 0);
        invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);

        // 绘制 the table.
        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();

        // 绘制 the mallets.
        positionObjectInScene(0f, mallet.height / 2f, -0.4f);
        colorProgram.useProgram();
        colorProgram.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f);
        mallet.bindData(colorProgram);
        mallet.draw();

        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y,
                blueMalletPosition.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f);
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        //注意，我们不必再次定义对象数据 -- 我们只是再次绘制相同的木槌，但在不同的位置并用不同的颜色。
        mallet.draw();

        // 绘制 the puck.
        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z);
        colorProgram.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f);
        puck.bindData(colorProgram);
        puck.draw();
    }

    private void positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        // 这个桌子是用x和y坐标定义的，所以我们旋转它。90度平躺在XZ平面上。
        setIdentityM(modelMatrix, 0);
        rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }

    // The mallets and the puck are positioned on the same plane as the table.
    // 木槌和冰球跟桌子在相同的平面上
    private void positionObjectInScene(float x, float y, float z) {
        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, x, y, z);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix,
                0, modelMatrix, 0);
    }
}