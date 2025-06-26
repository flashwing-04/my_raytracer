package math.geometry;

import math.*;

public class Ray {

    private Vec3 p;
    private Vec3 v;

    public Ray(Vec3 p, Vec3 v) {
        this.p = p;
        this.v = v.normalize();
    }

    public Vec3 getPoint(float s) {
        return p.add(v.multiply(s));
    }

    public Vec3 getP() {
        return p;
    }

    public Vec3 getV() {
        return v;
    }

    public Ray transform(Mat4 matrix) {
        Vec3 newP = matrix.multiply(p, 1);
        Vec3 newV = matrix.multiply(v, 0).normalize();
        return new Ray(newP, newV);
    }
}
