package lighting;

import math.Vec3;
import stuff.Color;

/**
 * Represents a point light source in 3D space.
 * Supports jittering position for soft shadows.
 */
public class Light {

    private final Vec3 position;
    private final float intensity;
    private final float radius;
    private final Color color;

    /**
     * Constructs a new Light.
     *
     * @param position  Position of the light in world coordinates.
     * @param intensity Brightness/intensity of the light.
     * @param radius    Radius of the light's area for jittering/soft shadows.
     * @param color     Color of the light.
     */
    public Light(Vec3 position, float intensity, float radius, Color color) {
        this.position = position;
        this.intensity = intensity;
        this.radius = radius;
        this.color = color;
    }

    /**
     * Creates a copy of this light with a modified intensity.
     *
     * @param newIntensity The new intensity value.
     * @return A new Light instance with updated intensity.
     */
    public Light copyWithIntensity(float newIntensity) {
        return new Light(position, newIntensity, radius, color);
    }

    /**
     * Generates a jittered position around the light's position for soft shadow sampling.
     * If the light is a SpotLight, jitter is applied in the plane perpendicular to the spot direction.
     * Otherwise, jitter is applied in the XY plane.
     *
     * @return A jittered position as Vec3.
     */
    public Vec3 jitterLightPosition() {
        Vec3 diskOffset = randomPointInDisk();

        if (this instanceof SpotLight spot) {
            Vec3 dir = spot.getDirection().normalize();

            // Choose an up vector that is not parallel to dir to build local coordinate frame
            Vec3 up = Math.abs(dir.getY()) < 0.99f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);

            Vec3 right = up.cross(dir).normalize();
            Vec3 localUp = dir.cross(right).normalize();

            // Offset position in plane at an angle of 90° to dir
            return position.add(right.multiply(diskOffset.getX()))
                    .add(localUp.multiply(diskOffset.getY()));
        }

        // For non-spot lights, jitter in XY plane
        return position.add(diskOffset);
    }

    /**
     * Samples a random point uniformly distributed inside a disk of radius `this.radius`.
     *
     * Uses polar coordinates with sqrt(random) for uniform distribution.
     *
     * @return Vec3 offset vector (X, Y) within disk, Z=0.
     */
    private Vec3 randomPointInDisk() {
        double r = radius * Math.sqrt(Math.random());
        double theta = 2.0 * Math.PI * Math.random();

        return new Vec3((float) (r * Math.cos(theta)), (float) (r * Math.sin(theta)), 0f);
    }

    /** Returns the position of the light. */
    public Vec3 getP() {
        return position;
    }

    /** Returns the intensity of the light. */
    public float getIntensity() {
        return intensity;
    }

    /** Returns the radius of the light (used for jittering). */
    public float getR() {
        return radius;
    }

    /** Returns the color of the light. */
    public Color getColor() {
        return color;
    }
}