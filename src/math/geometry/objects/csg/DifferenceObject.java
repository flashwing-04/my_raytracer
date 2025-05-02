package math.geometry.objects.csg;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import stuff.*;

import java.util.*;

public class DifferenceObject extends SceneObject {

    private SceneObject objA;
    private SceneObject objB;

    public DifferenceObject(SceneObject objA, SceneObject objB, Material material) {
        super(material);
        this.objA = objA;
        this.objB = objB;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersectionsA = objA.intersect(ray);
        List<Intersection> intersectionsB = objB.intersect(ray);

        List<Intersection> combined = new ArrayList<>();
        combined.addAll(intersectionsA);
        combined.addAll(intersectionsB);

        combined.sort(Comparator.comparingDouble(Intersection::getDistance));

        return filterDifferenceIntervals(combined);
    }

    private List<Intersection> filterDifferenceIntervals(List<Intersection> intersections) {
        List<Intersection> result = new ArrayList<>();

        boolean insideA = false;
        boolean insideB = false;
        boolean wasInside = false;

        for (Intersection inter : intersections) {
            SceneObject obj = inter.getObject();
            if (obj == objA) insideA = !insideA;
            else if (obj == objB) insideB = !insideB;

            boolean isInside = insideA && !insideB;
            if (isInside != wasInside) {
                Vec3 normal = (obj == objB) ? inter.getNormal().multiply(-1) : inter.getNormal();
                result.add(new Intersection(
                        inter.getPoint(),
                        normal,
                        inter.getDistance(),
                        this
                ));
            }
            wasInside = isInside;
        }

        return result;
    }

    public Vec3 getNormal(Vec3 p) {
        throw new UnsupportedOperationException("Use normal from Intersection instead.");
    }
}
