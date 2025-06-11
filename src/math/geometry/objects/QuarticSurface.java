package math.geometry.objects;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

public class QuarticSurface extends SceneObject{

    private float b, c;
    private Mat4 transform, inverseTransform;

    public QuarticSurface(float b, float c, Material material) {
        super(material);
        this.b = b;
        this.c = c;
        this.transform = new Mat4();
        this.inverseTransform = new Mat4().inverse();
    }

    public QuarticSurface(float b, float c, Mat4 transform, Material material) {
        super(material);
        this.b = b;
        this.c = c;
        this.transform = transform;
        this.inverseTransform = transform.inverse();
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();
        Ray localRay = ray.transform(inverseTransform);

        float t = 0f;
        float maxDistance = 100f;
        float epsilon = 1e-4f;
        int maxSteps = 1024;

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
            t += distance * 0.2f;
        }

        return intersections;
    }

    public float estimateDistance(Vec3 p) {
        float x = p.getX(), y = p.getY(), z = p.getZ();
        float x2 = x * x, y2 = y * y, z2 = z * z;
        float x4 = x2 * x2, y4 = y2 * y2, z4 = z2 * z2;

        float f = x4 + y4 + z4 + b * (x2 + y2 + z2) + c;

        float dfdx = 4f * x * x * x + 2f * b * x;
        float dfdy = 4f * y * y * y + 2f * b * y;
        float dfdz = 4f * z * z * z + 2f * b * z;

        float gradLength = (float)Math.sqrt(dfdx * dfdx + dfdy * dfdy + dfdz * dfdz);
        if (gradLength < 1e-6f) gradLength = 1e-6f;

        return f / gradLength;
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
        float x = point.getX(), y = point.getY(), z = point.getZ();
        float f = (float)(Math.pow(x, 4) + Math.pow(y, 4) + Math.pow(z, 4) + b * (x*x + y*y + z*z) + c);
        return f < 0f;
    }

    public SceneObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new QuarticSurface(b, c, newTransform, getMaterial());
    }
}
