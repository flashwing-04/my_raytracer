package math.geometry.objects.csg;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import stuff.*;

/**
 * Represents a Constructive Solid Geometry (CSG) intersection operation (A ∩ B).
 *
 * <p>This object represents the volume that is common to both input objects A and B.
 * Only points that are inside both A and B are considered part of the resulting object.</p>
 *
 * <p>This class defines how intersections and normals are calculated when combining
 * two scene objects through intersection.</p>
 */
public class IntersectionObject extends CSGObject {

    /**
     * Constructs a CSG intersection object (A ∩ B).
     *
     * @param objA     The first input object.
     * @param objB     The second input object.
     * @param material The material to apply to the resulting shape.
     */
    public IntersectionObject(SceneObject objA, SceneObject objB, Material material) {
        super(objA, objB, material);
    }

    /**
     * Determines whether a point is inside the intersection of A and B.
     * It is inside only if it is inside both.
     *
     * @param insideA True if the point is inside object A.
     * @param insideB True if the point is inside object B.
     * @return True if inside the intersection shape.
     */
    @Override
    protected boolean computeIsInside(boolean insideA, boolean insideB) {
        return insideA && insideB;
    }

    /**
     * Computes whether the ray origin is considered inside the intersection.
     *
     * @param insideA True if the ray starts inside A.
     * @param insideB True if the ray starts inside B.
     * @return True if inside the intersection shape.
     */
    @Override
    protected boolean computeWasInside(boolean insideA, boolean insideB) {
        return insideA && insideB;
    }

    /**
     * Adjusts the surface normal at an intersection point.
     * For intersection, the normal is used as-is from the intersected object.
     *
     * @param inter The intersection.
     * @param obj   The object that was hit (objA or objB).
     * @return The surface normal at the intersection point.
     */
    @Override
    protected Vec3 getAdjustedNormal(Intersection inter, SceneObject obj) {
        return inter.normal();
    }

    /**
     * Determines if a point is inside the intersection of the two objects.
     *
     * @param point The point to test.
     * @return True if the point lies inside both A and B.
     */
    @Override
    public boolean isInside(Vec3 point) {
        return objA.isInside(point) && objB.isInside(point);
    }

    /**
     * Applies a transformation to both input objects and returns a new intersection object.
     *
     * @param transformMatrix The transformation matrix to apply.
     * @return A new {@code IntersectionObject} with transformed components.
     */
    @Override
    public IntersectionObject transform(Mat4 transformMatrix) {
        return new IntersectionObject(
                objA.transform(transformMatrix),
                objB.transform(transformMatrix),
                getMaterial()
        );
    }
}