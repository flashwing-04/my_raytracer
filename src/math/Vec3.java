package math;

/**
 * Represents a 3D vector and provides common vector operations.
 * This class is immutable and supports operations like addition, subtraction,
 * dot product, cross product, normalization, reflection, refraction, and sampling.
 */
public class Vec3 {

    /** Constant representing the zero vector (0, 0, 0). */
    public static final Vec3 ZERO = new Vec3(0f, 0f, 0f);

    /** Constant representing the one vector (1, 1, 1). */
    public static final Vec3 ONE = new Vec3(1f, 1f, 1f);

    private final float x;
    private final float y;
    private final float z;

    /**
     * Constructs a 3D vector with specified x, y, z components.
     *
     * @param x The x-component.
     * @param y The y-component.
     * @param z The z-component.
     */
    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Constructs a vector with all components set to the same value.
     *
     * @param a The value to set x, y, and z.
     */
    public Vec3(float a) {
        this(a, a, a);
    }

    /**
     * Returns the length (magnitude) of this vector.
     *
     * @return The length of the vector.
     */
    public float getLength() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns the squared length of this vector.
     * This is faster than {@link #getLength()} as it avoids a square root.
     *
     * @return The squared length.
     */
    public float getLengthSquared() {
        float sqrt = (float) Math.sqrt(x * x + y * y + z * z);
        return sqrt * sqrt;
    }

    /**
     * Returns the result of subtracting the given vector from this vector.
     *
     * @param v The vector to subtract.
     * @return A new Vec3 representing the difference.
     */
    public Vec3 subtract(Vec3 v) {
        return new Vec3(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Returns the result of adding the given vector to this vector.
     *
     * @param v The vector to add.
     * @return A new Vec3 representing the sum.
     */
    public Vec3 add(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Multiplies this vector by a scalar.
     *
     * @param a The scalar value.
     * @return A new Vec3 scaled by the scalar.
     */
    public Vec3 multiply(float a) {
        return new Vec3(x * a, y * a, z * a);
    }

    /**
     * Performs component-wise multiplication with another vector.
     *
     * @param other The other vector.
     * @return A new Vec3 representing the product.
     */
    public Vec3 multiply(Vec3 other) {
        return new Vec3(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    /**
     * Divides this vector by a scalar.
     *
     * @param a The scalar value.
     * @return A new Vec3 representing the quotient.
     */
    public Vec3 divide(float a) {
        return new Vec3(x / a, y / a, z / a);
    }

    /**
     * Performs component-wise division with another vector.
     *
     * @param other The other vector.
     * @return A new Vec3 representing the quotient.
     */
    public Vec3 divide(Vec3 other) {
        return new Vec3(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    /**
     * Normalizes the vector to unit length.
     *
     * @return A new Vec3 with length 1, pointing in the same direction.
     */
    public Vec3 normalize() {
        float length = getLength();
        return new Vec3(x / length, y / length, z / length);
    }

    /**
     * Computes the dot product with another vector.
     *
     * @param v The other vector.
     * @return The scalar dot product.
     */
    public float dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Computes the cross product with another vector.
     *
     * @param v The other vector.
     * @return A new Vec3 perpendicular to both vectors.
     */
    public Vec3 cross(Vec3 v) {
        float nx = y * v.z - z * v.y;
        float ny = z * v.x - x * v.z;
        float nz = x * v.y - y * v.x;
        return new Vec3(nx, ny, nz);
    }

    /**
     * Linearly interpolates between this and another vector using a third vector as weights.
     *
     * @param y The target vector.
     * @param a A vector of interpolation weights for x, y, z.
     * @return The interpolated vector.
     */
    public Vec3 mix(Vec3 y, Vec3 a) {
        return new Vec3(
                this.x * (1 - a.x) + y.x * a.x,
                this.y * (1 - a.y) + y.y * a.y,
                this.z * (1 - a.z) + y.z * a.z
        );
    }

    /**
     * Reflects this vector around a given normal.
     *
     * @param normal The normal to reflect across.
     * @return The reflected and normalized vector.
     */
    public Vec3 reflect(Vec3 normal) {
        return this.subtract(normal.multiply(2 * normal.dot(this))).normalize();
    }

    /**
     * Computes the refraction direction based on Snell's Law.
     *
     * @param normal The surface normal.
     * @param i1 The refractive index of the current medium.
     * @param i2 The refractive index of the target medium.
     * @return The refracted vector, or null if total internal reflection occurs.
     */
    public Vec3 refract(Vec3 normal, float i1, float i2) {
        Vec3 incident = this.normalize();
        float cosW1 = -incident.dot(normal);
        float i = i1 / i2;
        float radical = 1 - i * i * (1 - cosW1 * cosW1);

        if (radical < 0f) return null; // Total internal reflection

        float cosW2 = (float) Math.sqrt(radical);
        return incident.multiply(i).add(normal.multiply(i * cosW1 - cosW2)).normalize();
    }

    /**
     * Generates a random direction in the hemisphere oriented around this vector.
     * Uses cosine-weighted sampling for diffuse reflection.
     *
     * @return A normalized random vector in the hemisphere.
     */
    public Vec3 randomHemisphereDirection() {
        Vec3 w = this.normalize();
        Vec3 a = Math.abs(w.x) > 0.1f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 tang1 = a.cross(w).normalize();
        Vec3 tang2 = w.cross(tang1);

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

    /**
     * Samples a direction for glossy reflection around this vector based on roughness.
     *
     * @param normal The surface normal.
     * @param roughness A value between 0 (perfect mirror) and 1 (fully diffuse).
     * @return A direction vector representing the glossy reflection.
     */
    public Vec3 sampleGlossyDirection(Vec3 normal, float roughness) {
        float exponent = Math.max(1.0f, (1.0f - roughness) * 100.0f);

        float u1 = (float) Math.random();
        float u2 = (float) Math.random();
        float theta = (float) Math.acos(Math.pow(u1, 1.0f / (exponent + 1)));
        float phi = 2.0f * (float) Math.PI * u2;

        float x = (float) (Math.sin(theta) * Math.cos(phi));
        float y = (float) (Math.sin(theta) * Math.sin(phi));
        float z = (float) Math.cos(theta);

        Vec3 w = this.normalize();
        Vec3 u = (Math.abs(w.x) > 0.1f ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0)).cross(w).normalize();
        Vec3 v = w.cross(u);

        Vec3 sampleDir = u.multiply(x).add(v.multiply(y)).add(w.multiply(z)).normalize();

        if (sampleDir.dot(normal) < 0.0f) {
            sampleDir = sampleDir.multiply(-1);
        }

        return sampleDir;
    }

    /**
     * Returns the x-component of the vector.
     *
     * @return The x value.
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the y-component of the vector.
     *
     * @return The y value.
     */
    public float getY() {
        return y;
    }

    /**
     * Returns the z-component of the vector.
     *
     * @return The z value.
     */
    public float getZ() {
        return z;
    }

    /**
     * Returns a string representation of the vector.
     *
     * @return A string in the form (x, y, z).
     */
    @Override
    public String toString() {
        return String.format("(%.5f, %.5f, %.5f)", x, y, z);
    }
}