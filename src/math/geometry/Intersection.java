package math.geometry;

import math.*;
import math.geometry.objects.*;

public class Intersection implements Comparable<Intersection>{
    private Vec3 point;
    private Vec3 normal;
    private float distance;
    private SceneObject object;

    public Intersection(Vec3 point, Vec3 normal, float distance, SceneObject object) {
        this.point = point;
        this.normal = normal.normalize();
        this.distance = distance;
        this.object = object;
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

    public int compareTo(Intersection other) {
        return Float.compare(this.distance, other.distance);
    }
}
