package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;

/**
 * Represents a torus (doughnut shape) defined by a major and minor radius.
 * The torus is centered at the origin in local space.
 *
 * The signed distance function estimates distance from a point to the torus surface.
 */
public class Torus extends SDFObject {

    private final float majorRadius; // Radius from center of torus to center of tube
    private final float minorRadius; // Radius of the tube

    /**
     * Constructs a Torus with given radii and identity transform.
     *
     * @param majorRadius Distance from torus center to tube center.
     * @param minorRadius Radius of the tube.
     * @param material    Material assigned to the torus.
     */
    public Torus(float majorRadius, float minorRadius, Material material) {
        super(material, new Mat4());
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Constructs a Torus with given radii and a specified transform.
     *
     * @param majorRadius Distance from torus center to tube center.
     * @param minorRadius Radius of the tube.
     * @param transform   Transformation matrix applied to the torus.
     * @param material    Material assigned to the torus.
     */
    public Torus(float majorRadius, float minorRadius, Mat4 transform, Material material) {
        super(material, transform);
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    /**
     * Estimates the signed distance from a point in local space to the torus surface.
     *
     * @param localP Point in the local coordinate system.
     * @return Signed distance estimate to the torus surface.
     */
    @Override
    public float estimateDistance(Vec3 localP) {
        float x = localP.getX();
        float y = localP.getY();
        float z = localP.getZ();

        // Compute distance from point's projection on XZ plane to the circle defining the torus tube center
        float qx = (float) Math.sqrt(x * x + z * z) - majorRadius;
        float qy = y;

        return (float) Math.sqrt(qx * qx + qy * qy) - minorRadius;
    }

    /**
     * Returns a new Torus transformed by the given matrix.
     * The new transform is composed by multiplying the given matrix with the current transform.
     *
     * @param transformationMatrix Transformation to apply.
     * @return Transformed Torus object.
     */
    @Override
    public SDFObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new Torus(majorRadius, minorRadius, newTransform, getMaterial());
    }
}