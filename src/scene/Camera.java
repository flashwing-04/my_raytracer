package scene;

import math.*;

public class Camera {

    private Vec3 position, v, r, u;
    private float heightImgPlane, widthImgPlane, distanceImgPlane;

    public Camera(Vec3 position, Vec3 v , Vec3 r, float heightImgPlane, float widthImgPlane, float distanceImgPlane) {
        this.position = position;
        this.v = v.normalize();
        this.r = r.normalize();
        this.u = v.cross(r).normalize();

        this.heightImgPlane = heightImgPlane;
        this.widthImgPlane = widthImgPlane;
        this.distanceImgPlane = distanceImgPlane;
    }

    public Vec3 getPxStart() {
         return position
                .add(v.multiply(distanceImgPlane))
                .subtract(r.multiply(widthImgPlane / 2))
                .subtract(u.multiply(heightImgPlane / 2));
    }

    public Vec3 getPxRightStep(int resX) {
        return r.multiply(widthImgPlane / resX);
    }

    public Vec3 getPxUpStep(int resY) {
        return  u.multiply(heightImgPlane / resY);
    }

    public Vec3 getPosition() {
        return position;
    }

    public Vec3 getV() {
        return v;
    }

    public Vec3 getR() {
        return r;
    }

    public Vec3 getU() {
        return u;
    }

    public float getHeightImgPlane() {
        return heightImgPlane;
    }

    public float getWidthImgPlane() {
        return widthImgPlane;
    }

    public float getDistanceImgPlane() {
        return distanceImgPlane;
    }
}
