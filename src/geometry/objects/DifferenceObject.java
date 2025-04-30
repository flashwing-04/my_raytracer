package geometry.objects;

import geometry.Intersection;
import geometry.Ray;
import geometry.Vec3;
import stuff.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DifferenceObject extends SceneObject{

    private SceneObject object1;
    private SceneObject object2;

    public DifferenceObject(SceneObject object1, SceneObject object2, Material material) {
        super(material);
        this.object1 = object1;
        this.object2 = object2;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();

        List<Intersection> intersections1 = object1.intersect(ray);
        List<Intersection> intersections2 = object2.intersect(ray);

        Collections.sort(intersections1);
        Collections.sort(intersections2);

        boolean insideA = false;
        boolean insideB = false;

        List<Intersection> all = new ArrayList<>();
        all.addAll(intersections1);
        all.addAll(intersections2);
        Collections.sort(all);

        for (Intersection inter : all) {
            boolean isFromA = intersections1.contains(inter);

            boolean valid = (isFromA && !insideB) || (!isFromA && insideA);

            if (valid) {
                Vec3 normal = inter.getNormal();
                if (!isFromA) {
                    normal = normal.multiply(-1);
                }

                intersections.add(new Intersection(inter.getPoint(), normal, inter.getDistance(), this));
            }

            // Update Flags
            if (isFromA) {
                insideA = !insideA;
            } else {
                insideB = !insideB;
            }
        }
        return intersections;
    }



    public Vec3 getNormal(Vec3 p) {
        throw new UnsupportedOperationException("Use normal from Intersection instead.");
    }
}
