package geometry.objects;

import geometry.*;
import stuff.*;

import java.util.ArrayList;
import java.util.List;

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
}
