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
        final float maxDistance = 100f;
        final float epsilon = 1e-4f;
        final int maxSteps = 512;

        float prevT = 0f;
        Vec3 prevPoint = localRay.getPoint(prevT);
        float prevDist = estimateDistance(prevPoint);

        float lastIntersectionT = -Float.MAX_VALUE;

        while (t < maxDistance && intersections.size() < maxSteps) {
            Vec3 point = localRay.getPoint(t);
            float dist = estimateDistance(point);

            // detect surface crossing
            if ((prevDist > 0 && dist <= 0) || (prevDist < 0 && dist >= 0)) {
                // binary search to refine intersection
                float t0 = prevT;
                float t1 = t;
                float distT0 = prevDist;

                for (int i = 0; i < 8; i++) {
                    float midT = 0.5f * (t0 + t1);
                    Vec3 midPoint = localRay.getPoint(midT);
                    float midDist = estimateDistance(midPoint);

                    if ((distT0 > 0 && midDist <= 0) || (distT0 < 0 && midDist >= 0)) {
                        t1 = midT;
                    } else {
                        t0 = midT;
                        distT0 = midDist;
                    }
                }

                // only add if sufficiently far from last intersection
                if (t1 - lastIntersectionT > epsilon) {
                    Vec3 localPoint = localRay.getPoint(t1);
                    Vec3 worldPoint = transform.multiply(localPoint, 1);
                    Vec3 worldNormal = getNormal(localPoint);

                    // IMPORTANT: Use world-space distance for intersection
                    float worldDistance = worldPoint.subtract(ray.getP()).getLength();

                    intersections.add(new Intersection(worldPoint, worldNormal, worldDistance, this, getMaterial()));
                    lastIntersectionT = t1;
                }

                // move a bit beyond intersection
                t = t1 + 2 * epsilon;
                prevT = t;
                prevPoint = localRay.getPoint(prevT);
                prevDist = estimateDistance(prevPoint);
                continue;
            }

            prevT = t;
            prevDist = dist;

            t += Math.max(Math.abs(dist), epsilon) * 0.2f;
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

    public abstract SDFObject transform(Mat4 transformationMatrix);

}
