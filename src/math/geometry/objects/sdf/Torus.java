package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;


public class Torus extends SDFObject {
    private float majorRadius;
    private float minorRadius;

    public Torus(float majorRadius, float minorRadius, Material material) {
        super(material, new Mat4());
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    public Torus(float majorRadius, float minorRadius, Mat4 transform, Material material) {
        super(material, transform);
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    public float estimateDistance(Vec3 localP) {
        float x = localP.getX();
        float y = localP.getY();
        float z = localP.getZ();
        float qx = (float)Math.sqrt(x * x + z * z) - majorRadius;
        float qy = y;
        return (float)Math.sqrt(qx * qx + qy * qy) - minorRadius;
    }

    public boolean isInside(Vec3 worldPoint) {
        Vec3 localPoint = inverseTransform.multiply(worldPoint, 1);
        return estimateDistance(localPoint) < 0.0f;
    }
    public SDFObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new Torus(majorRadius, minorRadius, newTransform, getMaterial());
    }
}
