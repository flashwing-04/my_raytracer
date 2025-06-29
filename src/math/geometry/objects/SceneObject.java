package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

/**
 * Abstract base class for all scene objects.
 * Provides basic material property, intersection and normal computations,
 * and transformation support.
 */
public abstract class SceneObject {

    private Material material;

    /**
     * Creates a scene object with the specified material.
     *
     * @param material Material applied to this object.
     */
    public SceneObject(Material material) {
        this.material = material;
    }

    /**
     * Creates a scene object without material (null).
     */
    public SceneObject() {
        this.material = null;
    }

    /**
     * Returns the material associated with this object.
     *
     * @return Material instance, or null if none set.
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Computes the intersections of this object with a given ray.
     *
     * @param ray Ray to test against.
     * @return List of intersections (may be empty).
     */
    public abstract List<Intersection> intersect(Ray ray);

    /**
     * Computes the surface normal vector at a given point on the object.
     *
     * @param p Point on the surface.
     * @return Normalized surface normal vector.
     */
    public abstract Vec3 getNormal(Vec3 p);

    /**
     * Tests if a point is inside this object.
     *
     * @param point Point to test.
     * @return True if point is inside the object, false otherwise.
     */
    public abstract boolean isInside(Vec3 point);

    /**
     * Checks if this object occludes a ray within a specified maximum distance.
     * Useful for shadow ray tests.
     *
     * @param ray         Ray to test.
     * @param maxDistance Maximum distance along the ray to check for occlusion.
     * @return True if the ray is occluded within maxDistance, false otherwise.
     */
    public boolean isOccluding(Ray ray, float maxDistance) {
        List<Intersection> intersections = this.intersect(ray);
        for (Intersection intersection : intersections) {
            float dist = intersection.distance();
            if (dist > 1e-5f && dist < maxDistance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a new transformed instance of this scene object.
     *
     * @param transformationMatrix Transformation matrix to apply.
     * @return Transformed scene object.
     */
    public abstract SceneObject transform(Mat4 transformationMatrix);
}