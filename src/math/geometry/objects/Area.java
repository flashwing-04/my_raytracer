package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

public class Area extends SceneObject{

    private Vec3 normal;
    private float d;

    public Area(Vec3 normal, float d, Material material) {
        super(material);
        this.normal = normal.normalize();
        this.d = d;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        float dot = normal.dot(ray.getV());
        if(dot != 0) {
            float s = (d-dot)/dot;
            Vec3 point = ray.getPoint(s);

            intersections.add(new Intersection(point, normal, s, this));
        }

        return intersections;
    }

    public Vec3 getNormal(Vec3 p) {
        return normal;
    }

    public boolean isInside(Vec3 point) {
        return normal.dot(point) - d < 0;
    }

    public Area transform(Mat4 transformMatrix) {
        Vec3 pointOnPlane = normal.multiply(d);
        Vec3 transformedPoint = transformMatrix.transform(pointOnPlane);

        float[] m = transformMatrix.getValues();
        Vec3 transformedNormal = new Vec3(
                m[0] * normal.getX() + m[1] * normal.getY() + m[2] * normal.getZ(),
                m[4] * normal.getX() + m[5] * normal.getY() + m[6] * normal.getZ(),
                m[8] * normal.getX() + m[9] * normal.getY() + m[10] * normal.getZ()
        ).normalize();

        float newD = transformedNormal.dot(transformedPoint);

        return new Area(transformedNormal, newD, this.getMaterial());
    }
}
