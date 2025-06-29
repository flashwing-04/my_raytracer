package math.geometry.objects;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import stuff.Material;

import java.util.Collections;
import java.util.List;

/**
 * Represents a triangle geometric primitive in 3D space.
 *
 * <p>The triangle is defined by three vertices {@code a}, {@code b}, and {@code c},
 * and a single constant surface normal computed at construction.</p>
 *
 * <p>This class provides ray-triangle intersection using the Möller–Trumbore algorithm,
 * point containment tests using barycentric coordinates, and transformation support.</p>
 */
public class Triangle extends SceneObject {

    private final Vec3 a, b, c;
    private final Vec3 normal;

    /**
     * Constructs a triangle with given vertices and material.
     *
     * @param a First vertex of the triangle.
     * @param b Second vertex of the triangle.
     * @param c Third vertex of the triangle.
     * @param material Material assigned to this triangle.
     */
    public Triangle(Vec3 a, Vec3 b, Vec3 c, Material material) {
        super(material);
        this.a = a;
        this.b = b;
        this.c = c;
        this.normal = b.subtract(a).cross(c.subtract(a)).normalize();
    }

    /**
     * Performs ray-triangle intersection using the Möller–Trumbore algorithm.
     *
     * @param ray The ray to test for intersection.
     * @return A list containing a single {@link Intersection} if hit,
     *         or an empty list if no intersection occurs.
     */
    @Override
    public List<Intersection> intersect(Ray ray) {
        Vec3 edge1 = b.subtract(a);
        Vec3 edge2 = c.subtract(a);
        Vec3 h = ray.v().cross(edge2);
        float det = edge1.dot(h);

        if (Math.abs(det) < 1e-6f) return Collections.emptyList(); // Ray parallel to triangle

        float invDet = 1f / det;
        Vec3 s = ray.p().subtract(a);
        float u = invDet * s.dot(h);
        if (u < 0f || u > 1f) return Collections.emptyList();

        Vec3 q = s.cross(edge1);
        float v = invDet * ray.v().dot(q);
        if (v < 0f || (u + v) > 1f) return Collections.emptyList();

        float t = invDet * edge2.dot(q);
        if (t < 1e-4f) return Collections.emptyList(); // Intersection behind ray origin or too close

        Intersection hit = new Intersection(ray.p().add(ray.v().multiply(t)), normal, t, this, getMaterial());
        return List.of(hit);
    }

    /**
     * Returns the constant normal vector of this triangle.
     *
     * @param p Point on the surface (unused as normal is constant).
     * @return The normalized surface normal vector.
     */
    @Override
    public Vec3 getNormal(Vec3 p) {
        return normal;
    }

    /**
     * Returns whether the point is inside the volume of this shape.
     *
     * <p>For a single triangle, this always returns {@code false} because a triangle
     * is a 2D surface and does not enclose volume.</p>
     *
     * @param point The point to test.
     * @return Always {@code false}.
     */
    @Override
    public boolean isInside(Vec3 point) {
        return false; // Triangle is a surface, not a volume
    }

    /**
     * Returns a new {@code Triangle} transformed by the given matrix.
     *
     * @param matrix Transformation matrix to apply.
     * @return Transformed triangle instance.
     */
    @Override
    public SceneObject transform(Mat4 matrix) {
        return new Triangle(
                matrix.transform(a),
                matrix.transform(b),
                matrix.transform(c),
                getMaterial()
        );
    }

    /**
     * Checks whether a point lies on or near the surface of the triangle.
     *
     * <p>Uses barycentric coordinates to determine if the point is within the triangle
     * bounds, assuming coplanarity.</p>
     *
     * @param p Point to test.
     * @return {@code true} if the point is inside the triangle's boundaries, {@code false} otherwise.
     */
    public boolean contains(Vec3 p) {
        Vec3 v0 = b.subtract(a), v1 = c.subtract(a), v2 = p.subtract(a);
        float d00 = v0.dot(v0);
        float d01 = v0.dot(v1);
        float d11 = v1.dot(v1);
        float d20 = v2.dot(v0);
        float d21 = v2.dot(v1);
        float denom = d00 * d11 - d01 * d01;

        if (Math.abs(denom) < 1e-6f) return false;

        float v = (d11 * d20 - d01 * d21) / denom;
        float w = (d00 * d21 - d01 * d20) / denom;
        float u = 1f - v - w;

        return u >= 0 && v >= 0 && w >= 0;
    }
}