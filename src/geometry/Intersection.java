package geometry;

import geometry.objects.SceneObject;

public class Intersection {

    private SceneObject object;
    private Ray ray;
    private float s;
    private Vec3 sp;

    public Intersection(SceneObject object, Ray ray) {
        this.object = object;
        this.ray = ray;
        this.s = computeS();
        this.sp = computeSP();
    }

    private float computeS() {
        return this.object.computeIntersectionS(this.ray);
    }

    private Vec3 computeSP() {
        return ray.getP().add(ray.getV().multiply(s));
    }

    public SceneObject getObject() {
        return object;
    }

    public Ray getRay() {
        return ray;
    }

    public float getS() {
        return s;
    }

    public Vec3 getSp() {
        return sp;
    }
}
