package math.geometry.objects;

import math.*;
import math.geometry.Intersection;
import math.geometry.Ray;
import stuff.*;
import java.io.*;
import java.util.*;

/**
 * Represents a triangle mesh loaded from an OBJ file as a scene object.
 *
 * <p>This class supports loading vertex and face data from OBJ files,
 * triangulating faces on the fly (using fan triangulation), and performing
 * ray intersection tests against all triangles in the mesh.</p>
 *
 * <p>The mesh is stored internally as a list of {@link Triangle} objects,
 * each associated with the same material.</p>
 */
public class MeshObject extends SceneObject {

    private final List<Vec3> vertices = new ArrayList<>();  // Don't actually need them
    private final List<Triangle> triangles = new ArrayList<>();

    /**
     * Loads a mesh from the given OBJ file and applies the specified material to the entire mesh.
     *
     * @param file The OBJ file containing vertex and face data.
     * @param material The material to apply to all triangles in the mesh.
     * @throws IOException if the OBJ file cannot be read.
     */
    public MeshObject(File file, Material material) throws IOException {
        super(material);
        loadOBJ(file);
    }

    /**
     * Loads vertex and face data from the OBJ file.
     * Supports vertices (lines starting with "v ") and faces (lines starting with "f ").
     * Faces with more than three vertices are fan triangulated into multiple triangles.
     *
     * @param file The OBJ file to read.
     * @throws IOException if the file cannot be read.
     */
    private void loadOBJ(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("v ")) {
                String[] tokens = line.split("\\s+");
                float x = Float.parseFloat(tokens[1]);
                float y = Float.parseFloat(tokens[2]);
                float z = Float.parseFloat(tokens[3]);
                vertices.add(new Vec3(x, y, z));
            } else if (line.startsWith("f ")) {
                String[] tokens = line.split("\\s+");
                int[] indices = new int[tokens.length - 1];
                for (int i = 0; i < indices.length; i++) {
                    String[] parts = tokens[i + 1].split("/");
                    indices[i] = Integer.parseInt(parts[0]) - 1; // OBJ indices are 1-based
                }

                // Fan triangulation: create triangles (v0, vi, vi+1) for i in 1..n-2
                Vec3 v0 = vertices.get(indices[0]);
                for (int i = 1; i < indices.length - 1; i++) {
                    Vec3 v1 = vertices.get(indices[i]);
                    Vec3 v2 = vertices.get(indices[i + 1]);
                    triangles.add(new Triangle(v0, v1, v2, getMaterial()));
                }
            }
        }
        reader.close();
    }

    /**
     * Intersects a ray with the mesh.
     *
     * @param ray The ray to intersect with the mesh.
     * @return List of all intersections with the mesh triangles.
     */
    @Override
    public List<Intersection> intersect(Ray ray) {
        List<Intersection> hits = new ArrayList<>();
        for (Triangle t : triangles) {
            hits.addAll(t.intersect(ray));
        }
        return hits;
    }

    /**
     * Gets the normal vector at the given point on the mesh.
     *
     * <p>If the point lies inside any triangle, returns that triangle's normal.
     * Otherwise, returns a default upward vector.</p>
     *
     * @param p Point on or near the mesh surface.
     * @return Normal vector at the point.
     */
    @Override
    public Vec3 getNormal(Vec3 p) {
        for (Triangle t : triangles) {
            if (t.contains(p)) return t.getNormal(p);
        }
        return new Vec3(0, 1, 0); // fallback normal
    }

    /**
     * Determines if a point is inside the mesh.
     *
     * <p>This uses a ray casting method: casts a ray in a fixed direction from the point,
     * counts intersections with mesh triangles, and returns true if the count is odd.</p>
     *
     * @param point The point to test.
     * @return True if the point is inside the mesh, false otherwise.
     */
    @Override
    public boolean isInside(Vec3 point) {
        for (Triangle tri : triangles) {
            if (tri.contains(point)) {
                return true;
            }
        }

        Vec3 direction = new Vec3(1, 0.5f, 0.3f).normalize();
        Ray ray = new Ray(point, direction);

        int count = 0;
        for (Triangle tri : triangles) {
            List<Intersection> hits = tri.intersect(ray);
            for (Intersection hit : hits) {
                if (hit.distance() > 1e-5f) {
                    count++;
                }
            }
        }
        return count % 2 == 1;
    }

    /**
     * Returns a transformed copy of this mesh object.
     *
     * <p>Transforms each triangle in the mesh by the given matrix and returns a new {@code MeshObject} instance.</p>
     *
     * @param matrix Transformation matrix to apply.
     * @return New {@code MeshObject} with transformed geometry.
     */
    @Override
    public SceneObject transform(Mat4 matrix) {
        List<Triangle> newTris = new ArrayList<>();
        for (Triangle t : triangles) {
            newTris.add((Triangle) t.transform(matrix));
        }
        MeshObject copy = new MeshObject(getMaterial());
        copy.triangles.addAll(newTris);
        copy.vertices.addAll(this.vertices);
        return copy;
    }

    /**
     * Private constructor used internally for cloning during transformation.
     */
    private MeshObject(Material material) {
        super(material);
    }
}