package lighting;

import math.*;
import stuff.*;

public class Light {

    public Vec3 p;
    public float intensity;
    public Color color;

    public Light(Vec3 p, float intensity, Color color) {
        this.p = p;
        this.intensity = intensity;
        this.color = color;
    }

    public Vec3 getP() {
        return p;
    }

    public float getIntensity() {
        return intensity;
    }

    public Color getColor() {
        return color;
    }

}
