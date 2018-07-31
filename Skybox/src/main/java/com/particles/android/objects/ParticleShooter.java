package com.particles.android.objects;

import com.particles.android.util.Geometry.*;

import java.util.Random;

import static android.opengl.Matrix.multiplyMV;
import static android.opengl.Matrix.setRotateEulerM;

/**
 * 粒子喷泉类
 */
public class ParticleShooter {
    private final Point position;
//    private final Vector direction;
    private final int color;

    private final float angleVariance;
    private final float speedVariance;

    private final Random random = new Random();

    private float[] rotationMatrix = new float[16];
    private float[] directionVector = new float[4];
    private float[] resultVector = new float[4];

    public ParticleShooter(Point position, Vector direction, int color,
                           float angleVarianceInDegrees, float speedVariance) {
        this.position = position;
//        this.direction = direction;
        this.color = color;
        this.angleVariance = angleVarianceInDegrees;
        this.speedVariance = speedVariance;

        directionVector[0] = direction.x;
        directionVector[1] = direction.y;
        directionVector[2] = direction.z;

    }

    public void addParticles(ParticleSystem particleSystem, float currentTime, int count) {
        for (int i = 0; i < count; i++) {

            // Matrix.setRotateEulerM()创建一个旋转矩阵，用angleVariance的一个随机变量改变发射角度，单位是度
            setRotateEulerM(rotationMatrix, 0,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance,
                    (random.nextFloat() - 0.5f) * angleVariance);
            // 让旋转矩阵与方向向量相乘，得到一个角度稍小的旋转向量
            multiplyMV(resultVector, 0,
                    rotationMatrix, 0,
                    directionVector, 0);
            // 调整速度
            float speedAdjustment = 1f + random.nextFloat() * speedVariance;
            // 向量分量随机调整
            Vector thisDirection = new Vector(
                    resultVector[0] * speedAdjustment,
                    resultVector[1] * speedAdjustment,
                    resultVector[2] * speedAdjustment
            );

            particleSystem.addParticle(position, color, thisDirection, currentTime);
        }
    }
}
