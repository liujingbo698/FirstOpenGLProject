package com.liu.airhockey.objects;

import com.liu.airhockey.data.VertexArray;
import com.liu.airhockey.programs.ColorShaderProgram;
import com.liu.airhockey.util.Geometry.Cylinder;
import com.liu.airhockey.util.Geometry.Point;

import static com.liu.airhockey.objects.ObjectBuilder.GeneratedData;

import java.util.List;

public class Puck {

    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float radius, height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Puck(float radius, float height, int numPointsAroundPuck) {

        GeneratedData generatedData = ObjectBuilder.createPuck(
                new Cylinder(new Point(0f, 0f, 0f), radius, height),
                numPointsAroundPuck);

        this.radius = radius;
        this.height = height;
        this.vertexArray = new VertexArray(generatedData.vertexData);
        this.drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0,
                colorProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    public void draw(){
        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
