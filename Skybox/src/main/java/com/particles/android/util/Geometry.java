/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.particles.android.util;

public class Geometry {
    public static class Point {
        public final float x, y, z;

        public Point(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Point translateY(float distance) {
            return new Point(x, y + distance, z);
        }

        public Point translate(Vector vector) {
            return new Point(
                x + vector.x,
                y + vector.y,
                z + vector.z);
        }
    }

    public static class Vector  {
        public final float x, y, z;

        public Vector(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public float length() {
            return (float) Math.sqrt(
                x * x
              + y * y
              + z * z);
        }

        // http://en.wikipedia.org/wiki/Cross_product向量叉乘
        public Vector crossProduct(Vector other) {
            return new Vector(
                (y * other.z) - (z * other.y),
                (z * other.x) - (x * other.z),
                (x * other.y) - (y * other.x));
        }
        // http://en.wikipedia.org/wiki/Dot_product
        public float dotProduct(Vector other) {
            return x * other.x
                 + y * other.y
                 + z * other.z;
        }

        public Vector scale(float f) {
            return new Vector(
                x * f,
                y * f,
                z * f);
        }
    }

    public static class Ray {
        public final Point point;
        public final Vector vector;

        public Ray(Point point, Vector vector) {
            this.point = point;
            this.vector = vector;
        }
    }

    // TODO: Re-use shared stuff in classes as an exercise

    public static class Circle {
        public final Point center;
        public final float radius;

        public Circle(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }

        public Circle scale(float scale) {
            return new Circle(center, radius * scale);
        }
    }

    public static class Cylinder {
        public final Point center;
        public final float radius;
        public final float height;

        public Cylinder(Point center, float radius, float height) {
            this.center = center;
            this.radius = radius;
            this.height = height;
        }
    }

    public static class Sphere {
        public final Point center;
        public final float radius;

        public Sphere(Point center, float radius) {
            this.center = center;
            this.radius = radius;
        }
    }

    public static class Plane {
        public final Point point;
        public final Vector normal;

        public Plane(Point point, Vector normal) {
            this.point = point;
            this.normal = normal;
        }
    }

    public static Vector vectorBetween(Point from, Point to) {
        return new Vector(
            to.x - from.x,
            to.y - from.y,
            to.z - from.z);
    }

    public static boolean intersects(Sphere sphere, Ray ray) {
        return distanceBetween(sphere.center, ray) < sphere.radius;
    }

    // http://mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
    // Note that this formula treats Ray as if it extended infinitely past
    // either point.
    public static float distanceBetween(Point point, Ray ray) {
        // 定义两个向量指向球心，一个从射线原点，一个从远点
        Vector p1ToPoint = vectorBetween(ray.point, point);
        Vector p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point);

        // 叉积的长度给出了这两个向量作为边的平行四边形的假想的面积。
        // 平行四边形可以是认为是由两个三角形组成的，所以这和两个向量所定义的三角形面积的两倍是一样的。
        // http://en.wikipedia.org/wiki/Cross_product#Geometric_meaning
        float areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length();
        float lengthOfBase = ray.vector.length();

        // 三角形的面积等于（底边*高度）/ 2。
        // 换句话说，高度等于（面积×2）/底边。
        // 高就是这个三角形是从点到射线的距离。
        float distanceFromPointToRay = areaOfTriangleTimesTwo / lengthOfBase;
        return distanceFromPointToRay;
    }

    // http://en.wikipedia.org/wiki/Line-plane_intersection
    // This also treats rays as if they were infinite. It will return a
    // point full of NaNs if there is no intersection point.
    // 把光线视为无限大。如果没有焦点它将返回一个充满NaNs的点，
    public static Point intersectionPoint(Ray ray, Plane plane) {
        // 射线上一点和平面上一点组成的向量
        Vector rayToPlaneVector = vectorBetween(ray.point, plane.point);

        float scaleFactor = rayToPlaneVector.dotProduct(plane.normal)
                          / ray.vector.dotProduct(plane.normal);

        Point intersectionPoint = ray.point.translate(ray.vector.scale(scaleFactor));
        return intersectionPoint;
    }
}
