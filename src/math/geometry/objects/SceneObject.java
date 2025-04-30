package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

public abstract class SceneObject {

    private Material material;

    public SceneObject(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public abstract List<Intersection> intersect(Ray ray);

    public abstract Vec3 getNormal(Vec3 p);

}
