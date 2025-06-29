package math.geometry.objects.csg;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.objects.SceneObject;
import stuff.Material;

/**
 * Represents a Constructive Solid Geometry (CSG) union operation (A ∪ B).
 *
 * <p>This object represents the combined volume of two scene objects A and B.
 * A point is considered inside the resulting object if it is inside either A or B.</p>
 *
 * <p>This class defines the intersection logic and normal adjustment for
 * ray tracing operations involving unioned geometry.</p>
 */
public class UnionObject extends CSGObject {

    /**
     * Constructs a CSG union object (A ∪ B).
     *
     * @param objA     The first input object.
     * @param objB     The second input object.
     * @param material The material to apply to the resulting shape.
     */
    public UnionObject(SceneObject objA, SceneObject objB, Material material) {
        super(objA, objB, material);
    }

    /**
     * Determines whether a point is inside the union of A and B.
     * It is inside if it is inside either one.
     *
     * @param insideA True if the point is inside object A.
     * @param insideB True if the point is inside object B.
     * @return True if inside the union shape.
     */
    @Override
    protected boolean computeIsInside(boolean insideA, boolean insideB) {
        return insideA || insideB;
    }

    /**
     * Computes whether the ray origin is considered inside the union.
     *
     * @param insideA True if the ray starts inside A.
     * @param insideB True if the ray starts inside B.
     * @return True if inside the union shape.
     */
    @Override
    protected boolean computeWasInside(boolean insideA, boolean insideB) {
        return insideA || insideB;
    }

    /**
     * Adjusts the surface normal at an intersection point.
     * For union, the normal is used as-is from the intersected object.
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
     * Determines if a point is inside the union of the two objects.
     *
     * @param point The point to test.
     * @return True if the point lies inside A or B.
     */
    @Override
    public boolean isInside(Vec3 point) {
        return objA.isInside(point) || objB.isInside(point);
    }

    /**
     * Applies a transformation to both input objects and returns a new union object.
     *
     * @param transformMatrix The transformation matrix to apply.
     * @return A new {@code UnionObject} with transformed components.
     */
    @Override
    public UnionObject transform(Mat4 transformMatrix) {
        return new UnionObject(
                objA.transform(transformMatrix),
                objB.transform(transformMatrix),
                getMaterial()
        );
    }
}