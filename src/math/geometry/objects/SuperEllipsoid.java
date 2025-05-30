package math.geometry.objects;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import stuff.Material;

import java.util.ArrayList;
import java.util.List;

public class SuperEllipsoid extends SceneObject{

    private float a1, a2, a3; // xRadius, yRadius, zRadius
    private float e1, e2;

    private Mat4 transform;
    private Mat4 inverseTransform;

    public SuperEllipsoid(float a1, float a2, float a3, float e1, float e2, Material material) {
        super(material);
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.e1 = e1;
        this.e2 = e2;
        this.transform = new Mat4();
        this.inverseTransform = new Mat4().inverse();
    }

    public SuperEllipsoid(float a1, float a2, float a3, float e1, float e2, Mat4 transform, Material material) {
        super(material);
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.e1 = e1;
        this.e2 = e2;
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
        float x = rayOrigin.getX() + s * rayDirection.getX();
        float y = rayOrigin.getY() + s * rayDirection.getY();
        float z = rayOrigin.getZ() + s * rayDirection.getZ();
        float term1 = (float) (Math.pow(Math.abs(x / a1), 2.0f / e2) + Math.pow(Math.abs(y / a2), 2.0f / e2));
        float term2 = (float) Math.pow(term1, e2 / e1);
        float term3 = (float) Math.pow(Math.abs(z / a3), 2.0f / e1);
        return term2 + term3 - 1.0f;
    }

    public Vec3 getNormal(Vec3 p) {
        float x = p.getX(), y = p.getY(), z = p.getZ();
        float x_ = x / a1, y_ = y / a2, z_ = z / a3;

        float epsilon = 1e-3f;
        float safeX = Math.abs(x_) < epsilon ? epsilon * Math.signum(x_) : x_;
        float safeY = Math.abs(y_) < epsilon ? epsilon * Math.signum(y_) : y_;
        float safeZ = Math.abs(z_) < epsilon ? epsilon * Math.signum(z_) : z_;

        float S = (float)(Math.pow(Math.abs(safeX), 2.0f / e2) + Math.pow(Math.abs(safeY), 2.0f / e2));
        float Sexp = (e2 / e1) - 1.0f;
        float common = (e2 / e1) * (float)Math.pow(S, Sexp);

        float nx = (2.0f / e2) * Math.signum(safeX) * (float)Math.pow(Math.abs(safeX), (2.0f / e2) - 1.0f) * common / a1;
        float ny = (2.0f / e2) * Math.signum(safeY) * (float)Math.pow(Math.abs(safeY), (2.0f / e2) - 1.0f) * common / a2;
        float nz = (2.0f / e1) * Math.signum(safeZ) * (float)Math.pow(Math.abs(safeZ), (2.0f / e1) - 1.0f) / a3;

        Vec3 localNormal = new Vec3(nx, ny, nz).normalize();
        Mat4 normalMatrix = transform.inverse().transpose();
        return normalMatrix.multiply(localNormal, 0).normalize();
    }

    public boolean isInside(Vec3 point) {
        float x = point.getX(), y = point.getY(), z = point.getZ();
        float term1 = (float) (Math.pow(Math.abs(x / a1), 2.0f / e2) + Math.pow(Math.abs(y / a2), 2.0f / e2));
        float term2 = (float) Math.pow(term1, e2 / e1);
        float term3 = (float) Math.pow(Math.abs(z / a3), 2.0f / e1);
        return term2 + term3 < 1.0f;
    }

    public SceneObject transform(Mat4 transformationMatrix) {
        Mat4 newTransform = transformationMatrix.multiply(this.transform);
        return new SuperEllipsoid(a1, a2, a3, e1, e2, newTransform, getMaterial());
    }
}
