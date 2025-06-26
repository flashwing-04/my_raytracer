package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;


public class SuperEllipsoid extends SDFObject {

    private float a1, a2, a3; // xRadius, yRadius, zRadius
    private float e1, e2;

    public SuperEllipsoid(float a1, float a2, float a3, float e1, float e2, Material material) {
        super(material, new Mat4());
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.e1 = e1;
        this.e2 = e2;
    }

    public SuperEllipsoid(float a1, float a2, float a3, float e1, float e2, Mat4 transform, Material material) {
        super(material, transform);
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.e1 = e1;
        this.e2 = e2;
    }

    public float estimateDistance(Vec3 localP) {

        float x = Math.abs(localP.getX() / a1);
        float y = Math.abs(localP.getY() / a2);
        float z = Math.abs(localP.getZ() / a3);

        return (float) Math.pow( Math.pow(Math.pow(x, 2.0f/e2) + Math.pow(y, 2.0f/e2), e2/e1) + Math.pow(z, 2.0f/e1), e1/2.0f) - 1.0f;
    }

    public boolean isInside(Vec3 worldPoint) {
        Vec3 localPoint = inverseTransform.multiply(worldPoint, 1);
        return estimateDistance(localPoint) < 0.0f;
    }

    public SDFObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new SuperEllipsoid(a1, a2, a3, e1, e2, newTransform, getMaterial());
    }
}
