package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

public class Sphere extends SceneObject {

    private Vec3 center;
    private float radius;

    public Sphere(Vec3 center, float radius, Material material) {
        super(material);
        this.center = center;
        this.radius = radius;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        Vec3 rayOrigin = ray.getP();
        Vec3 rayDirection = ray.getV();

        float a =rayDirection.dot(rayDirection);
        float b = 2 * (rayOrigin.dot(rayDirection) + (-1 * (rayDirection.dot(center))));
        float c = rayOrigin.dot(rayOrigin) + center.dot(center) - 2 *(center.dot(rayOrigin)) - (radius * radius);

        if (a == 0 && b != 0) {
            Vec3 point = ray.getPoint(-c / b);
            Vec3 normal = getNormal(point);
            intersections.add(new Intersection(point, normal, -c / b, this));

            return intersections;
        }

        float discriminant = b * b - 4 * a * c;

        if(discriminant > 1e-6f) {
            float sqrtDiscriminant = (float) Math.sqrt(discriminant);
            float k = (b < 1e-6f) ? (-b - sqrtDiscriminant) / 2f : (-b + sqrtDiscriminant) / 2f;
            float s1 = k / a;
            float s2 = c / k;

            Vec3 point1 = ray.getPoint(s1);
            Vec3 point2 = ray.getPoint(s2);

            Vec3 normal1 = getNormal(point1);
            Vec3 normal2 = getNormal(point2);

            intersections.add(new Intersection(point1, normal1, s1, this));
            intersections.add(new Intersection(point2, normal2, s2, this));
        }

        return intersections;
    }

    public Vec3 getNormal(Vec3 point) {
        return point.subtract(center).normalize();
    }

    public Vec3 getCenter() {
        return center;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isInside(Vec3 point) {
        Vec3 diff = point.subtract(center);
        float distanceSquared = diff.dot(diff);
        return distanceSquared < radius * radius;
    }

    public SceneObject transform(Mat4 transformMatrix) {
        float cx = center.getX(); float cy = center.getY(); float cz = center.getZ();
        float r2 = radius * radius;
        float[] coeffs = new float[]{1, 1, 1, 0, 0, 0, -cx, -cy, -cz, cx*cx + cy*cy + cz*cz - r2};

        return new Quadrik(coeffs, this.getMaterial()).transform(transformMatrix);
    }
}