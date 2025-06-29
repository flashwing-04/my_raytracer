package math;

public class Vec3 {

    public static final Vec3 ZERO = new Vec3(0f, 0f, 0f);
    public static final Vec3 ONE = new Vec3(1f, 1f, 1f);

    private final float x;
    private final float y;
    private final float z;

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

    public float getLength() {
        return (float) Math.sqrt(x*x + y*y + z*z);
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

    public Vec3 multiply(Vec3 other) { return new Vec3(this.x * other.x,this.y * other.y,this.z * other.z); }

    public Vec3 divide(float a) {
        return new Vec3(x/a,y/a,z/a);
    }

    public Vec3 divide(Vec3 other) { return new Vec3(this.x/ other.x,this.y/ other.y,this.z/ other.z); }

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

    public Vec3 mix(Vec3 y, Vec3 a) {
        return new Vec3(
                this.x * (1 - a.getX()) + y.getX() * a.getX(),
                this.y * (1 - a.getY()) + y.getY() * a.getY(),
                this.z * (1 - a.getZ()) + y.getZ() * a.getZ()
        );
    }

    public Vec3 reflect(Vec3 normal) {
        return this.subtract(normal.multiply(2 * normal.dot(this))).normalize();
    }

    public Vec3 refract(Vec3 normal, float i1, float i2) {
        Vec3 incident = this.normalize();
        float cosW1 = -incident.dot(normal);
        float i = i1/i2;

        float radical = 1 - i * i * (1 - cosW1 * cosW1);

        if (radical < 0f) return null;
        float cosW2 = (float) Math.sqrt(Math.max(0, radical));

        return incident.multiply(i).add(normal.multiply(i * cosW1 - cosW2)).normalize();
    }

    public Vec3 randomHemisphereDirection() {
        // Create tangent space basis (tangent1, tangent2, normal(this))
        Vec3 w = this.normalize();
        Vec3 a = Math.abs(w.getX()) > 0.1f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);

        Vec3 tang1 = a.cross(w).normalize();
        Vec3 tang2 = w.cross(tang1);

        // Generate random direction in hemisphere using cosine-weighted sampling
        double sinTheta = Math.sqrt(Math.random());
        double cosTheta = Math.sqrt(1 - sinTheta * sinTheta);

        double psi = Math.random() * 2.0 * Math.PI;

        double aComp = sinTheta * Math.cos(psi);
        double bComp = sinTheta * Math.sin(psi);

        Vec3 v1 = tang1.multiply((float) aComp);
        Vec3 v2 = tang2.multiply((float) bComp);
        Vec3 v3 = w.multiply((float) cosTheta);

        return v1.add(v2).add(v3).normalize();
    }

    public Vec3 sampleGlossyDirection(Vec3 normal, float roughness) {
        // Map roughness [0, 1] to Phong exponent [100, 1]
        float exponent = Math.max(1.0f, (1.0f - roughness) * 100.0f);

        float u1 = (float) Math.random();
        float u2 = (float) Math.random();
        float theta = (float) Math.acos(Math.pow(u1, 1.0f / (exponent + 1)));
        float phi = 2.0f * (float) Math.PI * u2;

        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float) (Math.sin(theta) * Math.sin(phi));
        float z = (float) Math.cos(theta);

        // Build orthonormal basis around reflectionDir
        Vec3 w = this.normalize();
        Vec3 u = (Math.abs(w.getX()) > 0.1f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0)).cross(w).normalize();
        Vec3 v = w.cross(u);

        // Sample direction in local space, then convert to world space
        Vec3 sampleDir = u.multiply(x).add(v.multiply(y)).add(w.multiply(z)).normalize();

        // Ensure sample is above the surface
        if (sampleDir.dot(normal) < 0.0f) {
            sampleDir = sampleDir.multiply(-1);
        }

        return sampleDir;
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

    public String toString() {
        return String.format("(%.5f, %.5f, %.5f)", getX(), getY(), getZ());
    }
}
