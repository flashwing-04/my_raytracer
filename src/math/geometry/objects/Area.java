package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

/**
 * Represents an infinite plane defined by a normalized normal vector and a distance from origin.
 * The plane equation is: normal · p = d
 */
public class Area extends SceneObject {

    private final Vec3 normal;
    private final float d;

    /**
     * Constructs a plane with given normal and distance from origin.
     *
     * @param normal   Normal vector of the plane (will be normalized).
     * @param d        Distance from origin along the normal.
     * @param material Material assigned to the plane.
     */
    public Area(Vec3 normal, float d, Material material) {
        super(material);
        this.normal = normal.normalize();
        this.d = d;
    }

    /**
     * Computes intersections of the ray with the plane.
     * Returns a list with zero or one intersection.
     *
     * @param ray Ray to intersect with.
     * @return List of intersections (empty or one element).
     */
    @Override
    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        float denom = normal.dot(ray.v());
        if (Math.abs(denom) > 1e-4f) { // Avoid division by zero (parallel rays)
            float t = (d - normal.dot(ray.p())) / denom;
            if (t >= 0) {
                Vec3 point = ray.getPoint(t);
                intersections.add(new Intersection(point, normal, t, this, getMaterial()));
            }
        }

        return intersections;
    }

    /**
     * Returns the constant plane normal vector.
     *
     * @param p Point on the plane (ignored).
     * @return Normalized plane normal vector.
     */
    @Override
    public Vec3 getNormal(Vec3 p) {
        return normal;
    }

    /**
     * Checks if a point is on the negative side of the plane.
     *
     * @param point Point in world space.
     * @return True if point is "inside" (negative half-space).
     */
    @Override
    public boolean isInside(Vec3 point) {
        return normal.dot(point) - d < 0;
    }

    /**
     * Returns a new Area plane transformed by the given matrix.
     * Transforms both the normal and a point on the plane.
     *
     * @param transformMatrix Transformation matrix to apply.
     * @return Transformed Area plane.
     */
    @Override
    public Area transform(Mat4 transformMatrix) {
        Vec3 pointOnPlane = normal.multiply(d); // A point on the plane
        Vec3 transformedPoint = transformMatrix.multiply(pointOnPlane, 1);

        // Extract rotation part from matrix to transform normal
        float[] m = transformMatrix.getValues();
        Vec3 transformedNormal = new Vec3(
                m[0] * normal.getX() + m[1] * normal.getY() + m[2] * normal.getZ(),
                m[4] * normal.getX() + m[5] * normal.getY() + m[6] * normal.getZ(),
                m[8] * normal.getX() + m[9] * normal.getY() + m[10] * normal.getZ()
        ).normalize();

        float newD = transformedNormal.dot(transformedPoint);

        return new Area(transformedNormal, newD, this.getMaterial());
    }
}