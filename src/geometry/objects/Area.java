package geometry.objects;

import geometry.*;
import stuff.*;

public class Area extends SceneObject{

    private Vec3 normal;
    private float d;

    public Area(Vec3 normal, float d, Material material) {
        super(material);
        this.normal = normal.normalize();
        this.d = d;
    }

    public float computeIntersectionS(Ray ray) {
        float dot = normal.dot(ray.getV());
        if(dot == 0) { return -1; }

        float s = (d-dot)/dot;

        return s;
    }

    public Vec3 getNormal(Vec3 p) {
        return normal;
    }
}
