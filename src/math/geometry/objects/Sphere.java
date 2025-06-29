package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

/**
 * Represents a geometric sphere defined by its center and radius.
 */
public class Sphere extends SceneObject {

    private Vec3 center;
    private float radius;

    /**
     * Constructs a Sphere object.
     *
     * @param center   Center of the sphere.
     * @param radius   Radius of the sphere.
     * @param material Surface material of the sphere.
     */
    public Sphere(Vec3 center, float radius, Material material) {
        super(material);
        this.center = center;
        this.radius = radius;
    }

    /**
     * Computes intersections of a ray with the sphere.
     *
     * @param ray The ray to test.
     * @return List of intersection points (0, 1, or 2).
     */
    @Override
    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        Vec3 origin = ray.p();
        Vec3 dir = ray.v();
        Vec3 oc = origin.subtract(center);

        float a = dir.dot(dir);
        float b = 2.0f * oc.dot(dir);
        float c = oc.dot(oc) - radius * radius;

        float discriminant = b * b - 4 * a * c;

        if (a == 0 && b != 0) {
            float t = -c / b;
            Vec3 point = ray.getPoint(t);
            intersections.add(new Intersection(point, getNormal(point), t, this, getMaterial()));
            return intersections;
        }

        if (discriminant > 1e-6f) {
            float sqrtD = (float) Math.sqrt(discriminant);
            float t1 = (-b - sqrtD) / (2.0f * a);
            float t2 = (-b + sqrtD) / (2.0f * a);

            Vec3 point1 = ray.getPoint(t1);
            Vec3 point2 = ray.getPoint(t2);

            intersections.add(new Intersection(point1, getNormal(point1), t1, this, getMaterial()));
            intersections.add(new Intersection(point2, getNormal(point2), t2, this, getMaterial()));
        }

        return intersections;
    }

    /**
     * Returns the normal vector at a given point on the surface.
     *
     * @param point Point on the sphere.
     * @return Unit normal vector at the given point.
     */
    @Override
    public Vec3 getNormal(Vec3 point) {
        return point.subtract(center).normalize();
    }

    /**
     * Returns the center of the sphere.
     */
    public Vec3 getCenter() {
        return center;
    }

    /**
     * Returns the radius of the sphere.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Checks whether a point is inside the sphere.
     *
     * @param point Point to check.
     * @return True if the point lies inside the sphere.
     */
    @Override
    public boolean isInside(Vec3 point) {
        return point.subtract(center).getLengthSquared() < radius * radius;
    }

    /**
     * Transforms the sphere using a transformation matrix by converting it into a quadric.
     *
     * @param transformMatrix The transformation to apply.
     * @return A transformed equivalent Quadric object.
     */
    @Override
    public SceneObject transform(Mat4 transformMatrix) {
        float cx = center.getX(), cy = center.getY(), cz = center.getZ();
        float r2 = radius * radius;

        // Construct the quadric representation of the sphere
        float[] coeffs = new float[]{
                1, 1, 1, 0, 0, 0, -cx, -cy, -cz, cx * cx + cy * cy + cz * cz - r2
        };

        return new Quadric(coeffs, getMaterial()).transform(transformMatrix);
    }
}