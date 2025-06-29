package math.geometry.objects;

import math.*;
import math.geometry.*;
import stuff.*;

import java.util.*;

/**
 * Represents a general quadric surface defined by a second-degree polynomial equation in 3D space.
 * The general form is:
 *   Ax² + By² + Cz² + 2Dxy + 2Exz + 2Fyz + 2Gx + 2Hy + 2Iz + J = 0
 */
public class Quadric extends SceneObject {

    private final float[] coefficients; // [A, B, C, D, E, F, G, H, I, J]

    /**
     * Constructs a quadric from its 10 coefficients.
     *
     * @param coefficients Array of 10 floats representing the quadric surface.
     * @param material     Material of the surface.
     */
    public Quadric(float[] coefficients, Material material) {
        super(material);
        if (coefficients.length != 10) {
            throw new IllegalArgumentException("Wrong number of coefficients!");
        }
        this.coefficients = coefficients;
    }

    /**
     * Constructs a quadric from a 4x4 symmetric matrix representation.
     *
     * @param mat      Matrix representing the quadric surface.
     * @param material Material of the surface.
     */
    public Quadric(Mat4 mat, Material material) {
        super(material);
        float[] m = mat.getValues();
        this.coefficients = new float[]{
                m[0], m[5], m[10], m[1], m[2], m[6], m[3], m[7], m[11], m[15]
        };
    }

    /**
     * Computes intersections of a ray with the quadric surface.
     *
     * @param ray The ray to intersect with.
     * @return List of intersections (0, 1, or 2).
     */
    @Override
    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        Vec3 o = ray.p();
        Vec3 d = ray.v();

        float vx = d.getX(), vy = d.getY(), vz = d.getZ();
        float px = o.getX(), py = o.getY(), pz = o.getZ();

        float a = coefficients[0], b = coefficients[1], c = coefficients[2];
        float d1 = coefficients[3], e = coefficients[4], f = coefficients[5];
        float g = coefficients[6], h = coefficients[7], i = coefficients[8], j = coefficients[9];

        float A = a * vx * vx + b * vy * vy + c * vz * vz
                + 2 * (d1 * vx * vy + e * vx * vz + f * vy * vz);

        float B = 2 * (
                a * px * vx + b * py * vy + c * pz * vz
                        + d1 * (px * vy + py * vx)
                        + e * (px * vz + pz * vx)
                        + f * (py * vz + pz * vy)
                        + g * vx + h * vy + i * vz
        );

        float C = a * px * px + b * py * py + c * pz * pz
                + 2 * (d1 * px * py + e * px * pz + f * py * pz
                + g * px + h * py + i * pz) + j;

        if (A == 0 && B != 0) {
            float t = -C / B;
            Vec3 point = ray.getPoint(t);
            Vec3 normal = getNormal(point);
            intersections.add(new Intersection(point, normal, t, this, getMaterial()));
            return intersections;
        }

        float discriminant = B * B - 4 * A * C;

        if (discriminant > 1e-6f) {
            float sqrtDiscriminant = (float) Math.sqrt(discriminant);
            float k = (B < 1e-6f) ? (-B - sqrtDiscriminant) / 2f : (-B + sqrtDiscriminant) / 2f;
            float t1 = k / A;
            float t2 = C / k;

            Vec3 point1 = ray.getPoint(t1);
            Vec3 point2 = ray.getPoint(t2);

            intersections.add(new Intersection(point1, getNormal(point1), t1, this, getMaterial()));
            intersections.add(new Intersection(point2, getNormal(point2), t2, this, getMaterial()));
        }

        return intersections;
    }

    /**
     * Checks if a point lies inside the quadric surface.
     *
     * @param point Point to test.
     * @return True if point satisfies the inequality Q(p) ≤ 0.
     */
    @Override
    public boolean isInside(Vec3 point) {
        float x = point.getX(), y = point.getY(), z = point.getZ();
        float val = coefficients[0] * x * x +
                coefficients[1] * y * y +
                coefficients[2] * z * z +
                2 * (coefficients[3] * x * y +
                        coefficients[4] * x * z +
                        coefficients[5] * y * z) +
                2 * (coefficients[6] * x +
                        coefficients[7] * y +
                        coefficients[8] * z) +
                coefficients[9];
        return val <= 1e-6f;
    }

    /**
     * Computes the normal vector at a point on the quadric surface.
     *
     * @param p Point on the surface.
     * @return Normalized surface normal vector.
     */
    @Override
    public Vec3 getNormal(Vec3 p) {
        float x = p.getX(), y = p.getY(), z = p.getZ();
        float nx = coefficients[0] * x + coefficients[3] * y + coefficients[4] * z + coefficients[6];
        float ny = coefficients[1] * y + coefficients[3] * x + coefficients[5] * z + coefficients[7];
        float nz = coefficients[2] * z + coefficients[4] * x + coefficients[5] * y + coefficients[8];
        return new Vec3(nx, ny, nz).normalize();
    }

    /**
     * Transforms the quadric surface using a transformation matrix.
     *
     * @param transformMatrix The transformation to apply.
     * @return A new transformed Quadric instance.
     */
    @Override
    public Quadric transform(Mat4 transformMatrix) {
        Mat4 Q = coefficientsToMat();
        Mat4 M_inv = transformMatrix.inverse();
        Mat4 M_invT = M_inv.transpose();
        Mat4 Q_transformed = M_invT.multiply(Q).multiply(M_inv);
        return new Quadric(Q_transformed, getMaterial());
    }

    /**
     * Converts the quadric coefficients into a 4x4 matrix representation.
     *
     * @return 4x4 matrix encoding the quadric surface.
     */
    public Mat4 coefficientsToMat() {
        float[] m = {
                coefficients[0], coefficients[3], coefficients[4], coefficients[6],
                coefficients[3], coefficients[1], coefficients[5], coefficients[7],
                coefficients[4], coefficients[5], coefficients[2], coefficients[8],
                coefficients[6], coefficients[7], coefficients[8], coefficients[9]
        };
        return new Mat4(m);
    }
}