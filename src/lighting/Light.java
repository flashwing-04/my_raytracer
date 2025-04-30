package lighting;

import math.*;
import stuff.*;

public class Light {

    public Vec3 p, l;
    public float intensity;
    public Color color;

    public Light(Vec3 p, Vec3 l, float intensity, Color color) {
        this.p = p;
        this.l = l.normalize();
        this.intensity = intensity;
        this.color = color;
    }

    public Vec3 getP() {
        return p;
    }

    public Vec3 getL() {
        return l;
    }

    public float getIntensity() {
        return intensity;
    }

    public Color getColor() {
        return color;
    }

}
