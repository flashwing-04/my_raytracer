package math.geometry;

import math.*;
import math.geometry.objects.*;
import stuff.Material;

public class Intersection implements Comparable<Intersection>{
    private Vec3 point;
    private Vec3 normal;
    private float distance;
    private SceneObject object;
    private Material material;

    public Intersection(Vec3 point, Vec3 normal, float distance, SceneObject object, Material material) {
        this.point = point;
        this.normal = normal.normalize();
        this.distance = distance;
        this.object = object;
        this.material = material;
    }

    public Vec3 getPoint() {
        return point;
    }

    public Vec3 getNormal() {
        return normal;
    }

    public float getDistance() {
        return distance;
    }

    public SceneObject getObject() {
        return object;
    }

    public Material getMaterial() {
        return material;
    }

    public int compareTo(Intersection other) {
        return Float.compare(this.distance, other.distance);
    }
}
