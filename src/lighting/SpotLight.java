package lighting;

import math.Vec3;
import stuff.Color;

/**
 * Represents a spotlight with position, direction, cutoff angle, and intensity falloff exponent.
 *
 * The spotlight attenuates light based on the angle between its direction and the vector to the point,
 * applying a smooth falloff controlled by the exponent parameter.
 *
 */
public class SpotLight extends Light {

    private final Vec3 direction;
    private final float angle;
    private final float exponent;

    /**
     * Constructs a new spotlight.
     *
     * @param p         Position of the spotlight in world coordinates.
     * @param intensity Light intensity scalar.
     * @param r         Radius or range of the light.
     * @param color     Color of the light.
     * @param direction Direction the spotlight is pointing (will be normalized).
     * @param angle     Cutoff angle (in radians) defining the spotlight cone.
     * @param exponent  Falloff exponent controlling the smoothness of the light's edge.
     */
    public SpotLight(Vec3 p, float intensity, float r, Color color, Vec3 direction, float angle, float exponent) {
        super(p, intensity, r, color);
        this.direction = direction.normalize();
        this.angle = angle;
        this.exponent = exponent;
    }

    /**
     * Creates a copy of this spotlight with a different intensity.
     *
     * @param newIntensity New intensity value.
     * @return A new SpotLight instance with the specified intensity.
     */
    public SpotLight copyWithIntensity(float newIntensity) {
        return new SpotLight(this.getP(), newIntensity, this.getR(), this.getColor(), this.direction, this.angle, this.exponent);
    }

    /**
     * Calculates the attenuation factor for a given point based on the spotlight direction and cutoff angle.
     * <p>
     * Returns 0 if the point is outside the spotlight cone.
     * </p>
     *
     * @param point The point in world space to calculate attenuation for.
     * @return Attenuation factor between 0 (no light) and 1 (full light).
     */
    public float getAttenuation(Vec3 point) {
        Vec3 toPoint = point.subtract(getP()).normalize();
        float cosTheta = direction.dot(toPoint);
        float cosAngle = (float) Math.cos(angle);

        float t = (cosTheta - cosAngle) / (1.0f - cosAngle);
        if (t <= 0f) return 0f;

        // Smooth falloff controlled by exponent
        return (float) Math.pow(t, 1.0f / exponent);
    }

    /** @return Normalized direction vector of the spotlight. */
    public Vec3 getDirection() {
        return direction;
    }

    /** @return Cutoff angle (in radians) of the spotlight cone. */
    public float getAngle() {
        return angle;
    }

    /** @return Falloff exponent controlling the smoothness of the light edge. */
    public float getExponent() {
        return exponent;
    }
}