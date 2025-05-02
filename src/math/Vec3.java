package math;

public class Vec3 {

    private float x;
    private float y;
    private float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(float a) {
        this.x = a;
        this.y = a;
        this.z = a;
    }

    public Vec3 subtract(Vec3 v) {
        return new Vec3(x-v.getX(),y-v.getY(),z-v.getZ());
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(x+v.getX(),y+v.getY(),z+v.getZ());
    }

    public Vec3 multiply(float a) {
        return new Vec3(x*a,y*a,z*a);
    }

    public Vec3 multiply(Vec3 other) {
        return new Vec3(
                this.x * other.x,
                this.y * other.y,
                this.z * other.z
        );
    }

    public Vec3 divide(float a) {
        return new Vec3(x/a,y/a,z/a);
    }

    public Vec3 divide(Vec3 other) {
        return new Vec3(
                this.x/ other.x,
                this.y/ other.y,
                this.z/ other.z
        );
    }

    public Vec3 normalize(){
        float length = this.getLength();
        return new Vec3(x/length,y/length,z/length);
    }

    public float dot(Vec3 v) {
        return x*v.getX() + y*v.getY() + z*v.getZ();
    }

    public Vec3 cross(Vec3 v) {
        float nx = y * v.getZ() - z * v.getY();
        float ny = z * v.getX() - x * v.getZ();
        float nz = x * v.getY() - y * v.getX();
        return new Vec3(nx, ny, nz);
    }

    public float getLength() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getZ(){
        return z;
    }
}
