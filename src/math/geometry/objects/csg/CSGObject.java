package math.geometry.objects.csg;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import math.geometry.objects.SceneObject;
import stuff.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Abstract base class for Constructive Solid Geometry (CSG) operations.
 *
 * CSG objects combine two {@link SceneObject}s (`objA` and `objB`) using boolean set operations
 * like union, intersection, and difference. This class handles the logic of ray intersection
 * filtering and inside/outside state transitions. Subclasses must define how boolean
 * operations work and how to adjust surface normals.
 *
 * <p>Subclasses include:
 * <ul>
 *     <li>{@code CSGUnion}</li>
 *     <li>{@code CSGIntersection}</li>
 *     <li>{@code CSGDifference}</li>
 * </ul>
 */
public abstract class CSGObject extends SceneObject {

    protected SceneObject objA, objB;

    /**
     * Constructs a CSG object with two scene objects and a material.
     *
     * @param objA     First operand object.
     * @param objB     Second operand object.
     * @param material Material applied to the final CSG shape.
     */
    public CSGObject(SceneObject objA, SceneObject objB, Material material) {
        super(material);
        this.objA = objA;
        this.objB = objB;
    }

    /**
     * Computes the intersections of the ray with the CSG object.
     * The method collects intersections from both operand objects, sorts them,
     * and filters them based on the CSG operation semantics.
     *
     * @param ray Ray to test against the object.
     * @return List of filtered intersections.
     */
    @Override
    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();
        intersections.addAll(objA.intersect(ray));
        intersections.addAll(objB.intersect(ray));
        intersections.sort(Comparator.comparingDouble(Intersection::distance));
        return filterIntersections(intersections, ray);
    }

    /**
     * Determines whether the ray has transitioned into or out of the CSG shape,
     * given the current inside states of objA and objB.
     *
     * @param insideA Whether the point is inside objA.
     * @param insideB Whether the point is inside objB.
     * @return Whether the combined shape is considered inside.
     */
    protected abstract boolean computeIsInside(boolean insideA, boolean insideB);

    /**
     * Determines the initial "wasInside" state from the starting point.
     *
     * @param insideA Whether the start is inside objA.
     * @param insideB Whether the start is inside objB.
     * @return Initial boolean value for state tracking.
     */
    protected abstract boolean computeWasInside(boolean insideA, boolean insideB);

    /**
     * Adjusts the surface normal depending on which object the intersection belongs to
     * and the nature of the CSG operation.
     *
     * @param inter The original intersection.
     * @param obj   The object it came from (objA or objB).
     * @return Adjusted normal vector.
     */
    protected abstract Vec3 getAdjustedNormal(Intersection inter, SceneObject obj);

    /**
     * Filters raw intersections using CSG logic to retain only those contributing
     * to the visible surface of the composite shape.
     *
     * @param intersections All intersections (sorted).
     * @param ray           The originating ray.
     * @return List of filtered intersections.
     */
    private List<Intersection> filterIntersections(List<Intersection> intersections, Ray ray) {
        List<Intersection> result = new ArrayList<>();

        // Slight offset to avoid precision errors
        Vec3 startPoint = ray.p().add(ray.v().multiply(1e-5f));
        boolean insideA = objA.isInside(startPoint);
        boolean insideB = objB.isInside(startPoint);
        boolean wasInside = computeWasInside(insideA, insideB);

        for (Intersection inter : intersections) {
            if (inter.distance() < 1e-5f) continue;

            SceneObject obj = inter.object();
            if (obj == objA) insideA = !insideA;
            else if (obj == objB) insideB = !insideB;

            boolean isInside = computeIsInside(insideA, insideB);
            if (isInside != wasInside) {
                result.add(new Intersection(
                        inter.point(),
                        getAdjustedNormal(inter, obj),
                        inter.distance(),
                        this,
                        obj.getMaterial()
                ));
            }

            wasInside = isInside;
        }

        return result;
    }

    /**
     * Not supported. For CSG objects, surface normals should be retrieved from intersections.
     *
     * @param p Point on the surface (unused).
     * @return Never returns; always throws.
     * @throws UnsupportedOperationException Use the normal from the {@link Intersection} object.
     */
    @Override
    public Vec3 getNormal(Vec3 p) {
        throw new UnsupportedOperationException("Use normal from Intersection instead.");
    }

    /**
     * Checks if a point lies inside the CSG object.
     * This method must be implemented in concrete CSG operations (e.g., union, intersection).
     *
     * @param point Point to test.
     * @return True if the point lies inside the composite shape.
     */
    @Override
    public abstract boolean isInside(Vec3 point);

    /**
     * Transforms the CSG object by applying a transformation matrix to both operands.
     *
     * @param transformMatrix The transformation matrix.
     * @return A new transformed CSG object.
     */
    @Override
    public abstract SceneObject transform(Mat4 transformMatrix);
}