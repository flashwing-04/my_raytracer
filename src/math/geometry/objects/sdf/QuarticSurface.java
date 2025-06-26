package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import stuff.Material;

public class QuarticSurface extends SDFObject {

    private float b, c;

    public QuarticSurface(float b, float c, Material material) {
        super(material, new Mat4());
        this.b = b;
        this.c = c;
    }

    public QuarticSurface(float b, float c, Mat4 transform, Material material) {
        super(material, transform);
        this.b = b;
        this.c = c;
    }

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

    public boolean isInside(Vec3 worldPoint) {
        Vec3 localPoint = inverseTransform.multiply(worldPoint, 1);
        return estimateDistance(localPoint) < 0.0f;
    }
    public SDFObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new QuarticSurface(b, c, newTransform, getMaterial());
    }
}
