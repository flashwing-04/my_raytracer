package math.geometry.objects.csg;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import stuff.*;


public class DifferenceObject extends CSGObject {

    public DifferenceObject(SceneObject objA, SceneObject objB, Material material) {
        super(objA, objB, material);
    }

    @Override
    protected boolean computeIsInside(boolean insideA, boolean insideB) {
        return insideA && !insideB;
    }

    @Override
    protected boolean computeWasInside(boolean insideA, boolean insideB) {
        return insideA && !insideB;
    }

    @Override
    protected Vec3 getAdjustedNormal(Intersection inter, SceneObject obj) {
        return (obj == objB) ? inter.getNormal().multiply(-1f) : inter.getNormal();
    }

    @Override
    public boolean isInside(Vec3 point) {
        return objA.isInside(point) && !objB.isInside(point);
    }

    @Override
    public DifferenceObject transform(Mat4 transformMatrix) {
        return new DifferenceObject(objA.transform(transformMatrix), objB.transform(transformMatrix), getMaterial());
    }
}
