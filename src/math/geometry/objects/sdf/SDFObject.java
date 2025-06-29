package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import math.geometry.objects.SceneObject;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for Signed Distance Function (SDF) based objects in the scene.
 *
 * SDFObjects represent implicit surfaces defined by a distance function estimating
 * the shortest distance from any point in space to the object's surface.
 * This class handles ray intersection by sphere tracing and provides normal computation.
 *
 *
 * Subclasses must implement {@link #estimateDistance(Vec3)} to define the shape,
 * {@link #isInside(Vec3)} to test point containment, and
 * {@link #transform(Mat4)} to create transformed copies.
 * <
 */
public abstract class SDFObject extends SceneObject {

    protected Mat4 transform, inverseTransform;

    /**
     * Constructs an SDF object with the given material and transformation.
     *
     * @param material  The material assigned to this object.
     * @param transform The transformation matrix from local to world space.
     *                  The inverse and transpose of the inverse are cached for efficiency.
     */
    public SDFObject(Material material, Mat4 transform) {
        super(material);
        this.transform = transform;
        this.inverseTransform = transform.inverse();
    }

    /**
     * Estimates the signed distance from the given point to the surface of this object.
     *
     * Positive values indicate points outside the surface,
     * negative values indicate points inside, and zero corresponds to the surface.
     *
     * @param point The query point in local object space.
     * @return The signed distance to the surface.
     */
    public abstract float estimateDistance(Vec3 point);

    /**
     * Performs ray intersection using sphere tracing.
     *
     * Transforms the ray to local space, marches along it using the distance estimator,
     * detects surface crossings, and refines intersections with binary search.
     * Returned intersections are in world space.
     *
     * @param ray The ray in world space.
     * @return A list of intersections along the ray with this object.
     */
    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();
        Ray localRay = ray.transform(inverseTransform);

        float t = 0f;
        final float maxDistance = 100f;
        final float epsilon = 1e-4f;
        final int maxSteps = 512;

        float prevT = 0f;
        Vec3 prevPoint = localRay.getPoint(prevT);
        float prevDist = estimateDistance(prevPoint);

        float lastIntersectionT = -Float.MAX_VALUE;

        while (t < maxDistance && intersections.size() < maxSteps) {
            Vec3 point = localRay.getPoint(t);
            float dist = estimateDistance(point);

            // Detect crossing from positive to negative or vice versa
            if ((prevDist > 0 && dist <= 0) || (prevDist < 0 && dist >= 0)) {
                // Refine intersection with binary search
                float t0 = prevT;
                float t1 = t;
                float distT0 = prevDist;

                for (int i = 0; i < 8; i++) {
                    float midT = 0.5f * (t0 + t1);
                    Vec3 midPoint = localRay.getPoint(midT);
                    float midDist = estimateDistance(midPoint);

                    if ((distT0 > 0 && midDist <= 0) || (distT0 < 0 && midDist >= 0)) {
                        t1 = midT;
                    } else {
                        t0 = midT;
                        distT0 = midDist;
                    }
                }

                // Add intersection if sufficiently far from the last one
                if (t1 - lastIntersectionT > epsilon) {
                    Vec3 localPoint = localRay.getPoint(t1);
                    Vec3 worldPoint = transform.multiply(localPoint, 1);
                    Vec3 worldNormal = getNormal(localPoint);

                    // Compute world-space distance along the original ray
                    float worldDistance = worldPoint.subtract(ray.p()).getLength();

                    intersections.add(new Intersection(worldPoint, worldNormal, worldDistance, this, getMaterial()));
                    lastIntersectionT = t1;
                }

                // Step forward beyond the intersection to continue searching
                t = t1 + 2 * epsilon;
                prevT = t;
                prevPoint = localRay.getPoint(prevT);
                prevDist = estimateDistance(prevPoint);
                continue;
            }

            prevT = t;
            prevDist = dist;

            t += Math.max(Math.abs(dist), epsilon) * 0.2f;
        }

        return intersections;
    }

    /**
     * Calculates the surface normal at a given point on the object surface.
     *
     * Uses central differences to approximate the gradient of the distance field,
     * then transforms the normal back to world space using the inverse transpose of the transform.
     *
     *
     * @param p The point in local object space.
     * @return The normalized surface normal in world space.
     */
    public Vec3 getNormal(Vec3 p) {
        float eps = 1e-4f;
        float dx = estimateDistance(new Vec3(p.getX() + eps, p.getY(), p.getZ())) - estimateDistance(new Vec3(p.getX() - eps, p.getY(), p.getZ()));
        float dy = estimateDistance(new Vec3(p.getX(), p.getY() + eps, p.getZ())) - estimateDistance(new Vec3(p.getX(), p.getY() - eps, p.getZ()));
        float dz = estimateDistance(new Vec3(p.getX(), p.getY(), p.getZ() + eps)) - estimateDistance(new Vec3(p.getX(), p.getY(), p.getZ() - eps));
        Vec3 localNormal = new Vec3(dx, dy, dz).normalize();
        Mat4 normalMatrix = transform.inverse().transpose();
        return normalMatrix.multiply(localNormal, 0).normalize();
    }

    /**
     * Tests whether a given point lies inside the object's surface.
     *
     * Transforms the world-space point to local space, then checks if the
     * signed distance is negative (inside).
     *
     * @param worldPoint The point to test, in world coordinates.
     * @return True if the point is inside the surface, false otherwise.
     */
    public boolean isInside(Vec3 worldPoint) {
        Vec3 localPoint = inverseTransform.multiply(worldPoint, 1);
        return estimateDistance(localPoint) < 0.0f;
    }


    /**
     * Returns a new instance of this object transformed by the given matrix.
     *
     * This allows immutable transformations without modifying the original object.
     *
     * @param transformationMatrix The transformation to apply.
     * @return A new transformed instance of this SDFObject.
     */
    public abstract SDFObject transform(Mat4 transformationMatrix);
}