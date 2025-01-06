package com.example.posturecheck;

import com.google.mlkit.vision.common.PointF3D;

public class Point3D {
    float x, y, z;

    public Point3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(PointF3D pointF3D) {
        this.x = pointF3D.getX();
        this.y = pointF3D.getY();
        this.z = pointF3D.getZ();
    }
}
