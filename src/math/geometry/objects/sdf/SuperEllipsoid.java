package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import math.geometry.objects.SceneObject;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

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

    public float estimateDistance(Vec3 p) {
        float x = Math.abs(p.getX() / a1);
        float y = Math.abs(p.getY() / a2);
        float z = Math.abs(p.getZ() / a3);

        return (float) Math.pow( Math.pow(Math.pow(x, 2.0f/e2) + Math.pow(y, 2.0f/e2), e2/e1) + Math.pow(z, 2.0f/e1), e1/2.0f) - 1.0f;
    }

    public boolean isInside(Vec3 point) {
        return estimateDistance(point) < 0.0f;
    }

    public SceneObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new SuperEllipsoid(a1, a2, a3, e1, e2, newTransform, getMaterial());
    }
}
