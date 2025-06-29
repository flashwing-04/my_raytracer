package lighting;

import math.*;
import stuff.*;

public class Light {

    public Vec3 p;
    public float intensity, r;
    public Color color;

    public Light(Vec3 p, float intensity, float r, Color color) {
        this.p = p;
        this.intensity = intensity;
        this.r = r;
        this.color = color;
    }

    public Light copyWithIntensity(float newIntensity) {
        return new Light(this.getP(), newIntensity, this.getR(), this.getColor());
    }

    public Vec3 jitterLightPosition() {
        Vec3 disk = randomPointInDisk();
        if (this instanceof SpotLight spot) {
            Vec3 dir = spot.getDirection();
            Vec3 up = Math.abs(dir.getY()) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            Vec3 right = up.cross(dir).normalize();
            Vec3 localUp = dir.cross(right).normalize();
            return this.p.add(right.multiply(disk.getX())).add(localUp.multiply(disk.getY()));
        }
        return this.p.add(disk);
    }

    private Vec3 randomPointInDisk() {
        double r = this.r * Math.sqrt(Math.random());
        double theta = 2 * Math.PI * Math.random();
        return new Vec3((float)(r * Math.cos(theta)), (float)(r * Math.sin(theta)), 0f);
    }

    public Vec3 getP() {
        return p;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getR() { return r; }

    public Color getColor() {
        return color;
    }

}
