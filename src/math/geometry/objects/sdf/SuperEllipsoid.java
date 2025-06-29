package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;

/**
 * Represents a superellipsoid defined by its radii and shape exponents.
 *
 * The implicit function for the superellipsoid is:
 *
 *  ((|x/a1|^(2/e2) + |y/a2|^(2/e2))^(e2/e1) + |z/a3|^(2/e1))^(e1/2) = 1
 *
 * This class provides a signed distance estimate for raymarching and intersection.
 */
public class SuperEllipsoid extends SDFObject {

    private final float a1, a2, a3;  // Radii along x, y, z axes
    private final float e1, e2;      // Shape exponents controlling squareness/roundness

    /**
     * Constructs a SuperEllipsoid with given radii and shape exponents, using identity transform.
     *
     * @param a1       Radius along the x-axis.
     * @param a2       Radius along the y-axis.
     * @param a3       Radius along the z-axis.
     * @param e1       Shape exponent controlling vertical roundness.
     * @param e2       Shape exponent controlling horizontal roundness.
     * @param material Material assigned to the superellipsoid.
     */
    public SuperEllipsoid(float a1, float a2, float a3, float e1, float e2, Material material) {
        super(material, new Mat4());
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.e1 = e1;
        this.e2 = e2;
    }

    /**
     * Constructs a SuperEllipsoid with given radii, shape exponents, and a transform.
     *
     * @param a1        Radius along the x-axis.
     * @param a2        Radius along the y-axis.
     * @param a3        Radius along the z-axis.
     * @param e1        Shape exponent controlling vertical roundness.
     * @param e2        Shape exponent controlling horizontal roundness.
     * @param transform Transformation matrix applied to the superellipsoid.
     * @param material  Material assigned to the superellipsoid.
     */
    public SuperEllipsoid(float a1, float a2, float a3, float e1, float e2, Mat4 transform, Material material) {
        super(material, transform);
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.e1 = e1;
        this.e2 = e2;
    }

    /**
     * Estimates the signed distance from a point in local space to the superellipsoid surface.
     *
     * @param localP Point in the local coordinate system.
     * @return Signed distance estimate to the surface.
     */
    @Override
    public float estimateDistance(Vec3 localP) {
        float x = Math.abs(localP.getX() / a1);
        float y = Math.abs(localP.getY() / a2);
        float z = Math.abs(localP.getZ() / a3);

        double horizontal = Math.pow(Math.pow(x, 2.0f / e2) + Math.pow(y, 2.0f / e2), e2 / e1);
        double value = Math.pow(horizontal + Math.pow(z, 2.0f / e1), e1 / 2.0f);

        return (float) (value - 1.0);
    }

    /**
     * Returns a new SuperEllipsoid transformed by the given matrix.
     * The new transform is composed by multiplying the given matrix with the current transform.
     *
     * @param transformationMatrix Transformation to apply.
     * @return Transformed SuperEllipsoid.
     */
    @Override
    public SDFObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new SuperEllipsoid(a1, a2, a3, e1, e2, newTransform, getMaterial());
    }
}