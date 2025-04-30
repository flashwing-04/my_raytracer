package geometry;

import geometry.objects.SceneObject;

public class Intersection {
    private Vec3 point;
    private float distance;
    private SceneObject object;

    public Intersection(Vec3 point, float distance, SceneObject object) {
        this.point = point;
        this.distance = distance;
        this.object = object;
    }

    public Vec3 getPoint() {
        return point;
    }

    public float getDistance() {
        return distance;
    }

    public SceneObject getObject() {
        return object;
    }
}

//public class Intersection {
//
//    private SceneObject object;
//    private Ray ray;
//    private float s;
//    private Vec3 sp;
//
//    public Intersection(SceneObject object, Ray ray) {
//        this.object = object;
//        this.ray = ray;
//        this.s = computeS();
//        this.sp = computeSP();
//    }
//
//    private float computeS() {
//        return this.object.computeIntersectionS(this.ray);
//    }
//
//    private Vec3 computeSP() {
//        return ray.getP().add(ray.getV().multiply(s));
//    }
//
//    public SceneObject getObject() {
//        return object;
//    }
//
//    public Ray getRay() {
//        return ray;
//    }
//
//    public float getS() {
//        return s;
//    }
//
//    public Vec3 getSp() {
//        return sp;
//    }
//}
