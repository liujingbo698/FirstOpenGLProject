uniform mat4 u_Matrix;
attribute vec3 a_Position;
varying vec3 v_Position;

void main()
{
    v_Position = a_Position;
    v_Position.z = -v_Position.z;

    gl_Position = u_Matrix * vec4(a_Position, 1.0);
    // 技巧：确保天空盒的每一部分都将位于归一化设备坐标的远平面上以及场景中的其他一切后面
    // 原因：透视除法把一切都除以w，并且w除以他自己，结果等于1，z最终在值为1的远平面上了
    gl_Position = gl_Position.xyww;
}