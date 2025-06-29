package math.geometry;

import math.*;
import math.geometry.objects.*;
import stuff.Material;

/**
 * Represents a geometric intersection between a ray and a scene object.
 */
public record Intersection(Vec3 point, Vec3 normal, float distance, SceneObject object, Material material) implements Comparable<Intersection> {

    /**
     * Constructs an Intersection instance.
     *
     * @param point    The intersection point in world space.
     * @param normal   The surface normal at the intersection (automatically normalized).
     * @param distance The distance along the ray to the intersection point.
     * @param object   The intersected scene object.
     * @param material The material of the intersected object.
     */
    public Intersection(Vec3 point, Vec3 normal, float distance, SceneObject object, Material material) {
        this.point = point;
        this.normal = normal.normalize();
        this.distance = distance;
        this.object = object;
        this.material = material;
    }

    /**
     * Compares two intersections based on distance from the ray origin.
     *
     * @param other The other intersection to compare against.
     * @return Negative if this is closer, positive if farther, zero if equal.
     */
    @Override
    public int compareTo(Intersection other) {
        return Float.compare(this.distance, other.distance);
    }
}