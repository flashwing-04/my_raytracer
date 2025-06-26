package math.geometry.objects.csg;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.Ray;
import math.geometry.objects.SceneObject;
import stuff.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class CSGObject extends SceneObject {

    protected SceneObject objA;
    protected SceneObject objB;

    public CSGObject(SceneObject objA, SceneObject objB, Material material) {
        super(material);
        this.objA = objA;
        this.objB = objB;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<>();
        intersections.addAll(objA.intersect(ray));
        intersections.addAll(objB.intersect(ray));
        intersections.sort(Comparator.comparingDouble(Intersection::getDistance));
        return filterIntersections(intersections, ray);
    }

    protected abstract boolean computeIsInside(boolean insideA, boolean insideB);
    protected abstract boolean computeWasInside(boolean insideA, boolean insideB);
    protected abstract Vec3 getAdjustedNormal(Intersection inter, SceneObject obj);

    private List<Intersection> filterIntersections(List<Intersection> intersections, Ray ray) {
        List<Intersection> result = new ArrayList<>();

        Vec3 startPoint = ray.getP().add(ray.getV().multiply(1e-5f));
        boolean insideA = objA.isInside(startPoint);
        boolean insideB = objB.isInside(startPoint);
        boolean wasInside = computeWasInside(insideA, insideB);

        for (Intersection inter : intersections) {
            if (inter.getDistance() < 1e-5f) continue;

            SceneObject obj = inter.getObject();
            if (obj == objA) insideA = !insideA;
            else if (obj == objB) insideB = !insideB;

            boolean isInside = computeIsInside(insideA, insideB);
            if (isInside != wasInside) {
                result.add(new Intersection(
                        inter.getPoint(),
                        getAdjustedNormal(inter, obj),
                        inter.getDistance(),
                        this,
                        obj.getMaterial()
                ));
            }

            wasInside = isInside;
        }

        return result;
    }

    @Override
    public Vec3 getNormal(Vec3 p) {
        throw new UnsupportedOperationException("Use normal from Intersection instead.");
    }

    @Override
    public abstract boolean isInside(Vec3 point);

    @Override
    public abstract SceneObject transform(Mat4 transformMatrix);
}
