package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;

/**
 * Represents a smooth union operation between two Signed Distance Function (SDF) objects.
 *
 * <p>This object models the union of two SDF shapes with smooth blending at their boundary,
 * controlled by a smoothness parameter. The smooth union allows soft transitions
 * instead of the sharp edges of classic boolean union.</p>
 *
 * <p>The distance estimate at any point is computed by a smooth minimum function of
 * the distances to the two objects.</p>
 */
public class SmoothUnionObject extends SDFObject {

    private final SDFObject objA, objB;
    private final float smoothness;

    /**
     * Constructs a smooth union SDF object with the given smoothness factor.
     *
     * @param objA       The first SDF object.
     * @param objB       The second SDF object.
     * @param material   The material of the resulting shape.
     * @param smoothness Controls the smooth blending width; higher values produce smoother transitions.
     */
    public SmoothUnionObject(SDFObject objA, SDFObject objB, Material material, float smoothness) {
        super(material, new Mat4());
        this.objA = objA;
        this.objB = objB;
        this.smoothness = smoothness;
    }

    /**
     * Constructs a smooth union SDF object with a specified transformation matrix.
     *
     * @param objA           The first SDF object.
     * @param objB           The second SDF object.
     * @param material       The material of the resulting shape.
     * @param smoothness     Smoothness factor for blending.
     * @param transform      Transformation matrix applied to this object.
     */
    public SmoothUnionObject(SDFObject objA, SDFObject objB, Material material, float smoothness, Mat4 transform) {
        super(material, transform);
        this.objA = objA;
        this.objB = objB;
        this.smoothness = smoothness;
    }

    /**
     * Estimates the signed distance from point {@code p} to the smooth union shape.
     *
     * <p>The method transforms the point into local coordinates of both child SDFs,
     * then calculates the smooth union using a smooth minimum function.</p>
     *
     * @param p The query point in world coordinates.
     * @return Estimated signed distance to the smooth union shape.
     */
    @Override
    public float estimateDistance(Vec3 p) {
        Vec3 localPA = objA.inverseTransform.multiply(p, 1);
        Vec3 localPB = objB.inverseTransform.multiply(p, 1);
        float dA = objA.estimateDistance(localPA);
        float dB = objB.estimateDistance(localPB);
        return smoothMin(dA, dB, smoothness);
    }

    /**
     * Returns a new transformed instance of this smooth union object.
     *
     * @param transformationMatrix Transformation matrix to apply.
     * @return New {@code SmoothUnionObject} with transformed child objects.
     */
    @Override
    public SDFObject transform(Mat4 transformationMatrix) {
        return new SmoothUnionObject(
                objA.transform(transformationMatrix),
                objB.transform(transformationMatrix),
                getMaterial(),
                smoothness,
                transformationMatrix);
    }

    /**
     * Computes a smooth minimum between two values with smoothing factor {@code k}.
     *
     * <p>This function smoothly blends between {@code a} and {@code b} over a transition width controlled by {@code k}.</p>
     *
     * @param a First value.
     * @param b Second value.
     * @param k Smoothing parameter; larger means smoother transition.
     * @return Smooth minimum of {@code a} and {@code b}.
     */
    private static float smoothMin(float a, float b, float k) {
        float h = Math.max(k - Math.abs(a - b), 0.0f) / k;
        return Math.min(a, b) - h * h * k * 0.25f;
    }
}