package com.example.posturecheck.util.vector;

import android.util.Log;

public class MathVector {
    Point3D point1, point2;
    public static float OrientationZ;
    public MathVector(int x1,int y1, int z1, int x2, int y2, int z2) {
        point1 = new Point3D(x1, y1, z1) {
        };
        point2 = new Point3D(x2, y2, z2) {
        };
    }

    public MathVector(Point3D p1, Point3D p2) {
        point1 = p1;
        point2 = p2;
    }

    public float width() {
        return point2.x - point1.x;
    }

    public float height() {
        return point2.y - point1.y;
    }
    public float depth() {
        return point2.z - point1.z;
    }

    public float length() {
        return (float) Math.sqrt(width()*width() + height()*height());
    }

    float scalarProduct(MathVector vector2) {
        return width()*vector2.width() + height()*vector2.height();
    }
    public float vectorAngle() {
        float res = (float) Math.toDegrees(Math.atan2( height(),width() ));
        Log.d("s",String.valueOf(res));
        return res;
    }

    public float vectorsAngle(MathVector vector2) {
        return (float) Math.toDegrees(Math.acos(scalarProduct(vector2)/(length()*vector2.length())));
    }

//    public float YRotation() {
//        return (float) Math.atan2(Math.abs(width()),Math.abs(depth()));
//    }
//
//    public float XRotation() {
//        return (float) Math.atan2(height(),width());
//    }

    public float ZRotation() {
        return (float) Math.cos(Math.atan2(depth(),width()));
    }


}
