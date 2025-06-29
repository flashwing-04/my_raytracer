package math.geometry.objects.csg;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import stuff.*;

/**
 * Represents a Constructive Solid Geometry (CSG) difference operation (A - B).
 *
 * <p>This operation subtracts object B from object A, keeping only the parts
 * of A that do not intersect with B. The resulting shape includes regions that
 * are inside A but outside B.</p>
 *
 * <p>This class overrides the necessary methods from {@link CSGObject} to define
 * how intersections and normals behave for the difference operation.</p>
 */
public class DifferenceObject extends CSGObject {

    /**
     * Constructs a CSG difference object A - B.
     *
     * @param objA     The minuend object (from which B is subtracted).
     * @param objB     The subtrahend object (to be subtracted from A).
     * @param material The material to assign to the resulting surface.
     */
    public DifferenceObject(SceneObject objA, SceneObject objB, Material material) {
        super(objA, objB, material);
    }

    /**
     * Determines whether the composite shape is considered "inside" at a transition point.
     * For difference, a point is inside the result if it is inside A but not inside B.
     *
     * @param insideA True if the point is inside object A.
     * @param insideB True if the point is inside object B.
     * @return True if inside the difference shape.
     */
    @Override
    protected boolean computeIsInside(boolean insideA, boolean insideB) {
        return insideA && !insideB;
    }

    /**
     * Computes the initial "was inside" state at the ray origin.
     *
     * @param insideA True if the origin is inside object A.
     * @param insideB True if the origin is inside object B.
     * @return True if the starting point is considered inside the difference shape.
     */
    @Override
    protected boolean computeWasInside(boolean insideA, boolean insideB) {
        return insideA && !insideB;
    }

    /**
     * Adjusts the normal direction depending on which object was hit.
     * If the surface belongs to B, the normal is inverted (flipped) to
     * reflect the subtraction operation.
     *
     * @param inter The original intersection.
     * @param obj   The object hit (objA or objB).
     * @return The adjusted surface normal.
     */
    @Override
    protected Vec3 getAdjustedNormal(Intersection inter, SceneObject obj) {
        return (obj == objB) ? inter.normal().multiply(-1f) : inter.normal();
    }

    /**
     * Determines whether a given point lies inside the result of A - B.
     *
     * @param point The point to test.
     * @return True if the point is inside A and not inside B.
     */
    @Override
    public boolean isInside(Vec3 point) {
        return objA.isInside(point) && !objB.isInside(point);
    }

    /**
     * Transforms both component objects using the provided matrix and
     * returns a new difference object with the same material.
     *
     * @param transformMatrix The transformation to apply.
     * @return A transformed {@code DifferenceObject}.
     */
    @Override
    public DifferenceObject transform(Mat4 transformMatrix) {
        return new DifferenceObject(
                objA.transform(transformMatrix),
                objB.transform(transformMatrix),
                getMaterial()
        );
    }
}