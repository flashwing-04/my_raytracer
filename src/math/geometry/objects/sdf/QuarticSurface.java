package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;

/**
 * Represents a quartic surface defined by the implicit equation:
 *
 *      f(x, y, z) = x^4 + y^4 + z^4 + b*(x^2 + y^2 + z^2) + c = 0
 *
 * Uses signed distance estimation for raymarching and intersection.
 */
public class QuarticSurface extends SDFObject {

    private final float b, c;

    /**
     * Constructs a QuarticSurface with parameters b, c and identity transform.
     *
     * @param b       Coefficient for quadratic terms.
     * @param c       Constant term.
     * @param material Material assigned to the surface.
     */
    public QuarticSurface(float b, float c, Material material) {
        super(material, new Mat4());
        this.b = b;
        this.c = c;
    }

    /**
     * Constructs a QuarticSurface with parameters b, c and a given transform.
     *
     * @param b         Coefficient for quadratic terms.
     * @param c         Constant term.
     * @param transform Transformation matrix applied to the surface.
     * @param material  Material assigned to the surface.
     */
    public QuarticSurface(float b, float c, Mat4 transform, Material material) {
        super(material, transform);
        this.b = b;
        this.c = c;
    }

    /**
     * Estimates the signed distance from a point in local space to the quartic surface.
     * Uses the gradient of the implicit function for normalization.
     *
     * @param localP Point in the local coordinate system.
     * @return Signed distance estimate to the surface.
     */
    @Override
    public float estimateDistance(Vec3 localP) {
        float x = localP.getX(), y = localP.getY(), z = localP.getZ();
        float x2 = x * x, y2 = y * y, z2 = z * z;
        float x4 = x2 * x2, y4 = y2 * y2, z4 = z2 * z2;

        float f = x4 + y4 + z4 + b * (x2 + y2 + z2) + c;

        float dfdx = 4f * x * x * x + 2f * b * x;
        float dfdy = 4f * y * y * y + 2f * b * y;
        float dfdz = 4f * z * z * z + 2f * b * z;

        float gradLength = (float) Math.sqrt(dfdx * dfdx + dfdy * dfdy + dfdz * dfdz);
        if (gradLength < 1e-6f) gradLength = 1e-6f;

        return f / gradLength;
    }

    /**
     * Returns a new QuarticSurface transformed by the given matrix.
     * The new transform is composed by multiplying the given matrix with the current transform.
     *
     * @param transformationMatrix Transformation to apply.
     * @return Transformed QuarticSurface.
     */
    @Override
    public SDFObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new QuarticSurface(b, c, newTransform, getMaterial());
    }
}