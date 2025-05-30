package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

public class SuperEllipsoidSphere extends SceneObject{

    private float radius;
    private float a;

    private Mat4 transform;
    private Mat4 inverseTransform;

    public SuperEllipsoidSphere(float radius, float a, Material material) {
        super(material);
        this.radius = radius;
        this.a = a;

        this.transform = new Mat4();
        this.inverseTransform = new Mat4().inverse();
    }

    public SuperEllipsoidSphere(float radius, float a, Mat4 transform, Material material) {
        super(material);
        this.radius = radius;
        this.a = a;

        this.transform = transform;
        this.inverseTransform = transform.inverse();
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        Ray localRay = ray.transform(inverseTransform);
        Vec3 rayOrigin = localRay.getP();
        Vec3 rayDirection = localRay.getV();

        float sMin = 0f;
        float sMax = 100f;
        int steps = 10000;
        float epsilon = 1e-6f;

        float prevS = sMin;
        float prevF = f(prevS, rayOrigin, rayDirection);

        for (int i = 1; i <= steps; i++) {
            float currS = sMin + (sMax - sMin) * i / steps;
            float currF = f(currS, rayOrigin, rayDirection);

            if (prevF * currF < 0) {
                // Bisection method
                float left = prevS, right = currS;
                float leftF = prevF, rightF = currF;
                while (right - left > epsilon) {
                    float mid = (left + right) / 2f;
                    float midF = f(mid, rayOrigin, rayDirection);
                    if (leftF * midF < 0) {
                        right = mid;
                        rightF = midF;
                    } else {
                        left = mid;
                        leftF = midF;
                    }
                }

                float sRoot = (left + right) / 2f;

                boolean isDuplicate = false;
                for (Intersection inter : intersections) {
                    if (Math.abs(inter.getDistance() - sRoot) < epsilon) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    Vec3 localPoint = localRay.getPoint(sRoot);
                    Vec3 worldPoint = transform.multiply(localPoint, 1);
                    Vec3 worldNormal = getNormal(localPoint);
                    intersections.add(new Intersection(worldPoint, worldNormal, sRoot, this));
                }
            }
            prevS = currS;
            prevF = currF;
        }
        return intersections;
    }

    private float f(float s, Vec3 rayOrigin, Vec3 rayDirection) {
        return (float) (Math.pow(Math.abs(rayOrigin.getX() + s * rayDirection.getX()), a)
                + Math.pow(Math.abs(rayOrigin.getY() + s * rayDirection.getY()), a)
                + Math.pow(Math.abs(rayOrigin.getZ() + s * rayDirection.getZ()), a)
                - Math.pow(radius, a));
    }

    public Vec3 getNormal(Vec3 point) {
        float nx = a * Math.signum(point.getX()) * (float)Math.pow(Math.abs(point.getX()), a - 1);
        float ny = a * Math.signum(point.getY()) * (float)Math.pow(Math.abs(point.getY()), a - 1);
        float nz = a * Math.signum(point.getZ()) * (float)Math.pow(Math.abs(point.getZ()), a - 1);
        Vec3 localNormal = new Vec3(nx, ny, nz).normalize();

        Mat4 normalMatrix = transform.inverse().transpose();
        return normalMatrix.multiply(localNormal,0).normalize();
    }

    public boolean isInside(Vec3 point) {
        float value = (float)(
                Math.pow(Math.abs(point.getX()), a) +
                Math.pow(Math.abs(point.getY()), a) +
                Math.pow(Math.abs(point.getZ()), a)
        );
        return value < Math.pow(radius, a);
    }

    public SceneObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new SuperEllipsoidSphere(this.radius, this.a, newTransform, this.getMaterial());
    }
}
