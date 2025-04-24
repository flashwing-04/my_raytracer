package geometry.objects;

import geometry.*;
import stuff.*;

public class Sphere extends SceneObject {

    private Vec3 p;
    private float radius;

    public Sphere(Vec3 p, float radius, Material material) {
        super(material);
        this.p = p;
        this.radius = radius;
    }

    public float computeIntersectionS(Ray ray) {
        Vec3 rayOrigin = ray.getP();
        Vec3 rayDirection = ray.getV();

        float a =rayDirection.dot(rayDirection);
        float b = 2 * (rayOrigin.dot(rayDirection) + (-1 * (rayDirection.dot(p))));
        float c = rayOrigin.dot(rayOrigin) + p.dot(p) - 2 *(p.dot(rayOrigin)) - (radius * radius);
        float discriminant = b * b - 4 * a * c;

        if(discriminant < 0) { return -1; }
        if(a == 0) { return -b/c; }

        float sqrtDisc = (float) Math.sqrt(discriminant);
        float k = (-b - (Math.signum(b) * sqrtDisc)) / 2.0f;
        float s1 = c/k;
        float s2 = k/a;

        return Math.min(s1, s2);
    }

    public Vec3 getNormal(Vec3 point) {
        return point.subtract(p).normalize();
    }

    public Vec3 getP() {
        return p;
    }

    public float getRadius() {
        return radius;
    }

}