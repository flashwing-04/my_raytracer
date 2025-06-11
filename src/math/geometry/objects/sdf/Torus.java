package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import math.geometry.objects.SceneObject;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

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

    public float estimateDistance(Vec3 p) {
        float x = p.getX();
        float y = p.getY();
        float z = p.getZ();
        float qx = (float)Math.sqrt(x * x + z * z) - majorRadius;
        float qy = y;
        return (float)Math.sqrt(qx * qx + qy * qy) - minorRadius;
    }

    public boolean isInside(Vec3 point) {
        return estimateDistance(point) < 0.0f;
    }

    public SceneObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new Torus(majorRadius, minorRadius, newTransform, getMaterial());
    }
}
