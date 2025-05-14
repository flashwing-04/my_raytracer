package lighting;

import math.Vec3;
import stuff.Color;

public class SpotLight extends Light {

    private Vec3 direction;
    private float angle, exponent;

    public SpotLight(Vec3 p, float intensity, Color color, Vec3 direction, float angle, float exponent) {
        super(p, intensity, color);
        this.direction = direction.normalize();
        this.angle = angle;
        this.exponent = exponent;
    }

    public SpotLight copyWithIntensity(float newIntensity) {
        return new SpotLight(this.getP(), newIntensity, this.getColor(), this.getDirection(), this.getAngle(), this.getExponent());
    }

    public float getAttenuation(Vec3 point) {
        Vec3 toPoint = point.subtract(getP()).normalize();

        float cosTheta = direction.dot(toPoint);
        float cosAngle = (float)Math.cos(angle);

        float t = (cosTheta - cosAngle) / (1.0f - cosAngle);

        if (t <= 0f) return 0f;
        return (float)Math.pow(t, 1.0f / exponent);
    }

    public Vec3 getDirection() { return direction; }

    public float getAngle() { return angle; }

    public float getExponent() { return exponent; }
}
