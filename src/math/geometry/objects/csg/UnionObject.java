package math.geometry.objects.csg;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import stuff.*;

import java.util.*;

public class UnionObject extends SceneObject {

    private SceneObject object1;
    private SceneObject object2;

    public UnionObject(SceneObject object1, SceneObject object2, Material material) {
        super(material);
        this.object1 = object1;
        this.object2 = object2;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        List<Intersection> intersections1 = object1.intersect(ray);
        List<Intersection> intersections2 = object2.intersect(ray);

        intersections.addAll(intersections1);
        intersections.addAll(intersections2);

        return intersections;
    }

    public Vec3 getNormal(Vec3 p) {
        throw new UnsupportedOperationException("Use normal from Intersection instead.");
    }
}
