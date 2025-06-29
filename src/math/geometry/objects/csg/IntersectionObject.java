package math.geometry.objects.csg;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import stuff.*;


public class IntersectionObject extends CSGObject {

    public IntersectionObject(SceneObject objA, SceneObject objB, Material material) {
        super(objA, objB, material);
    }

    @Override
    protected boolean computeIsInside(boolean insideA, boolean insideB) {
        return insideA && insideB;
    }

    @Override
    protected boolean computeWasInside(boolean insideA, boolean insideB) {
        return insideA && insideB;
    }

    @Override
    protected Vec3 getAdjustedNormal(Intersection inter, SceneObject obj) {
        return inter.normal();
    }

    @Override
    public boolean isInside(Vec3 point) {
        return objA.isInside(point) && objB.isInside(point);
    }

    @Override
    public IntersectionObject transform(Mat4 transformMatrix) {
        return new IntersectionObject(objA.transform(transformMatrix), objB.transform(transformMatrix), getMaterial());
    }
}

