package geometry;

public class Ray {

    private Vec3 p;
    private Vec3 v;

    public Ray(Vec3 p, Vec3 v) {
        this.p = p;
        this.v = v.normalize();
    }

    public Vec3 getP() {
        return p;
    }

    public Vec3 getV() {
        return v;
    }
}
