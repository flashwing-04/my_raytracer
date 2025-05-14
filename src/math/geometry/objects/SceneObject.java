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

    public SceneObject() { this.material = null; }

    public Material getMaterial() {
        return material;
    }

    public abstract List<Intersection> intersect(Ray ray);

    public abstract Vec3 getNormal(Vec3 p);

    public abstract boolean isInside(Vec3 point);

    public boolean isOccluding(Ray ray, float maxDistance) {
        List<Intersection> intersections = this.intersect(ray);
        for (Intersection intersection : intersections) {
            float dist = intersection.getDistance();
            if (dist > 1e-5f && dist < maxDistance) {
                return true;
            }
        }
        return false;
    }

    public abstract SceneObject transform(Mat4 transformationMatrix);
}
