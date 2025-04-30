package geometry.objects;

import geometry.*;
import stuff.*;

import java.util.ArrayList;
import java.util.List;

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

        float discriminant = b * b - 4 * a * c;

        if(discriminant > 0) {
            float sqrtDiscriminant = (float) Math.sqrt(discriminant);
            float s1 = (-b - sqrtDiscriminant) / (2.0f * a);
            float s2 = (-b + sqrtDiscriminant) / (2.0f * a);

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

}