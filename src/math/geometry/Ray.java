package math.geometry;

import math.*;
import math.geometry.objects.SceneObject;

import java.util.List;

/**
 * Represents a ray in 3D space, defined by an origin point and a normalized direction vector.
 */
public record Ray(Vec3 p, Vec3 v) {

    /**
     * Constructs a new Ray with the given origin and direction.
     *
     * @param p The origin point of the ray.
     * @param v The direction vector of the ray (automatically normalized).
     */
    public Ray(Vec3 p, Vec3 v) {
        this.p = p;
        this.v = v.normalize();
    }

    /**
     * Computes a point along the ray at a given distance `s`.
     *
     * @param s The scalar distance from the origin along the ray direction.
     * @return A point at distance `s` along the ray.
     */
    public Vec3 getPoint(float s) {
        return p.add(v.multiply(s));
    }

    /**
     * Transforms this ray using a given transformation matrix.
     *
     * @param matrix The matrix to apply.
     * @return A new transformed Ray.
     */
    public Ray transform(Mat4 matrix) {
        Vec3 newP = matrix.multiply(p, 1);
        Vec3 newV = matrix.multiply(v, 0).normalize();
        return new Ray(newP, newV);
    }

    /**
     * Finds the closest intersection between this ray and a list of scene objects.
     *
     * @param objects The list of scene objects to test for intersection.
     * @return The nearest valid intersection, or null if none are found.
     */
    public Intersection getNearestIntersection(List<SceneObject> objects) {
        Intersection nearestIntersection = null;
        float minDist = Float.MAX_VALUE;

        for (SceneObject obj : objects) {
            for (Intersection inter : obj.intersect(this)) {
                float dist = inter.distance();
                if (dist > 1e-4f && dist < minDist) {
                    minDist = dist;
                    nearestIntersection = inter;
                }
            }
        }

        return nearestIntersection;
    }
}