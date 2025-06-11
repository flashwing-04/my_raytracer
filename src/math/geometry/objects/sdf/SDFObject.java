package math.geometry.objects.sdf;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import math.geometry.objects.SceneObject;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

public abstract class SDFObject extends SceneObject {

    protected Mat4 transform, inverseTransform;

    public SDFObject(Material material, Mat4 transform) {
        super(material);
        this.transform = transform;
        this.inverseTransform = transform.inverse();
    }

    public abstract float estimateDistance(Vec3 point);

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

    public Vec3 getNormal(Vec3 p) {
        float eps = 1e-4f;
        float dx = estimateDistance(new Vec3(p.getX() + eps, p.getY(), p.getZ())) - estimateDistance(new Vec3(p.getX() - eps, p.getY(), p.getZ()));
        float dy = estimateDistance(new Vec3(p.getX(), p.getY() + eps, p.getZ())) - estimateDistance(new Vec3(p.getX(), p.getY() - eps, p.getZ()));
        float dz = estimateDistance(new Vec3(p.getX(), p.getY(), p.getZ() + eps)) - estimateDistance(new Vec3(p.getX(), p.getY(), p.getZ() - eps));
        Vec3 localNormal = new Vec3(dx, dy, dz).normalize();
        Mat4 normalMatrix = transform.inverse().transpose();
        return normalMatrix.multiply(localNormal, 0).normalize();
    }

    public abstract boolean isInside(Vec3 point);

    public abstract SceneObject transform(Mat4 transformationMatrix);

}
