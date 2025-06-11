package math.geometry.objects;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

public class Torus extends SceneObject {
    private float majorRadius;
    private float minorRadius;
    private Mat4 transform, inverseTransform;

    public Torus(float majorRadius, float minorRadius, Material material) {
        super(material);
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        this.transform = new Mat4();
        this.inverseTransform = new Mat4().inverse();
    }

    public Torus(float majorRadius, float minorRadius, Mat4 transform, Material material) {
        super(material);
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        this.transform = transform;
        this.inverseTransform = transform.inverse();
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();
        Ray localRay = ray.transform(inverseTransform);

        float t = 0f;
        float maxDistance = 100f;
        float epsilon = 1e-4f;
        int maxSteps = 256;

        for (int i = 0; i < maxSteps; i++) {
            Vec3 point = localRay.getPoint(t);
            float distance = estimateDistance(point);

            if (distance < epsilon) {
                Vec3 worldPoint = transform.multiply(point, 1);
                Vec3 worldNormal = getNormal(point);
                intersections.add(new Intersection(worldPoint, worldNormal, t, this));
                break;
            }

            if (t > maxDistance) break;
            t += distance * 0.8f;
        }
        return intersections;
    }

    private float estimateDistance(Vec3 p) {
        float x = p.getX();
        float y = p.getY();
        float z = p.getZ();
        float qx = (float)Math.sqrt(x * x + z * z) - majorRadius;
        float qy = y;
        return (float)Math.sqrt(qx * qx + qy * qy) - minorRadius;
    }

    public Vec3 getNormal(Vec3 p) {
        float eps = 1e-4f;
        float dx = estimateDistance(new Vec3(p.getX() + eps, p.getY(), p.getZ())) - estimateDistance(new Vec3(p.getX() - eps, p.getY(), p.getZ()));
        float dy = estimateDistance(new Vec3(p.getX(), p.getY() + eps, p.getZ())) - estimateDistance(new Vec3(p.getX(), p.getY() - eps, p.getZ()));
        float dz = estimateDistance(new Vec3(p.getX(), p.getY(), p.getZ() + eps)) - estimateDistance(new Vec3(p.getX(), p.getY(), p.getZ() - eps));
        Vec3 localNormal = new Vec3(dx, dy, dz).normalize();
        Mat4 normalMatrix = transform.inverse().transpose();
        return normalMatrix.multiply(localNormal, 0).normalize();
    }

    public boolean isInside(Vec3 point) {
        return estimateDistance(point) < 0.0f;
    }

    public SceneObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new Torus(majorRadius, minorRadius, newTransform, getMaterial());
    }
}
