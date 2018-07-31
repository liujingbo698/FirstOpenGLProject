package com.particles.android.objects;

import android.graphics.Color;

import com.particles.android.data.VertexArray;

import com.particles.android.programs.ParticleShaderProgram;
import com.particles.android.util.Geometry.*;

import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.glDrawArrays;
import static com.particles.android.Constants.BYTES_PER_FLOAT;

/**
 * 粒子系统类
 */
public class ParticleSystem {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int VECTOR_COMPONENT_COUNT = 3;
    private static final int PARTICLE_START_TIME_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT
                    + COLOR_COMPONENT_COUNT
                    + VECTOR_COMPONENT_COUNT
                    + PARTICLE_START_TIME_COMPONENT_COUNT;

    private static final int STRIDE = TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT;

    private final float[] particles;
    private final VertexArray vertexArray;
    private final int maxParticleCount;

    private int currentParticleCount;
    private int nextParticle;

    public ParticleSystem(int maxParticleCount) {
        // 创建分量数组 元素数量 * 单个元素的成分数
        particles = new float[maxParticleCount * TOTAL_COMPONENT_COUNT];
        vertexArray = new VertexArray(particles);
        this.maxParticleCount = maxParticleCount;
    }

    public void addParticle(Point position, int color, Vector direction, float particleStartTime) {
        // 计算每一个分量的偏移量
        final int particleOffset = nextParticle * TOTAL_COMPONENT_COUNT;
        // 当前粒子起始分量所在偏移位置
        int currentOffset = particleOffset;
        // 记录下一个元素编号
        nextParticle++;

        // 每添加一个新粒子，currentParticleCount数量加一个
        if (currentParticleCount < maxParticleCount) {
            currentParticleCount++;
        }
        // 当到达最大值时，从0开始以便回收最旧的粒子
        if (nextParticle == maxParticleCount) {
            nextParticle = 0;
        }

        particles[currentOffset++] = position.x;
        particles[currentOffset++] = position.y;
        particles[currentOffset++] = position.z;

        particles[currentOffset++] = Color.red(color) / 255f;
        particles[currentOffset++] = Color.green(color) / 255f;
        particles[currentOffset++] = Color.blue(color) / 255f;

        particles[currentOffset++] = direction.x;
        particles[currentOffset++] = direction.y;
        particles[currentOffset++] = direction.z;

        particles[currentOffset++] = particleStartTime;

        // 将这个新粒子复制到本地缓冲区
        // 只复制新数据，直接从偏移值开始记录单个成分数量个个数
        vertexArray.updateBuffer(particles, particleOffset, TOTAL_COMPONENT_COUNT);
    }

    public void bindData(ParticleShaderProgram particleProgram) {
        int dataOffset = 0;
        // 设置位置属性到顶点数组中
        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        dataOffset += POSITION_COMPONENT_COUNT;
        // 设置颜色属性到顶点数组中
        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT, STRIDE);
        dataOffset += COLOR_COMPONENT_COUNT;
        // 设置方向属性到顶点数组中
        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getDirectionVectorAttributeLocation(),
                VECTOR_COMPONENT_COUNT, STRIDE);
        dataOffset += VECTOR_COMPONENT_COUNT;
        // 设置开始时间属性到顶点数组中
        vertexArray.setVertexAttribPointer(dataOffset,
                particleProgram.getParticleStartTimeAttributeLocation(),
                PARTICLE_START_TIME_COMPONENT_COUNT, STRIDE);
    }

    public void draw(){
        glDrawArrays(GL_POINTS, 0, currentParticleCount);
    }

}
