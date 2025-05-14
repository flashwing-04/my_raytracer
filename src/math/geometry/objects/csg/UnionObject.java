package math.geometry.objects.csg;

import math.Mat4;
import math.Vec3;
import math.geometry.Intersection;
import math.geometry.objects.SceneObject;
import stuff.Material;


public class UnionObject extends CSGObject {

    public UnionObject(SceneObject objA, SceneObject objB, Material material) {
        super(objA, objB, material);
    }

    @Override
    protected boolean computeIsInside(boolean insideA, boolean insideB) {
        return insideA || insideB;
    }

    @Override
    protected boolean computeWasInside(boolean insideA, boolean insideB) {
        return insideA || insideB;
    }

    @Override
    protected Vec3 getAdjustedNormal(Intersection inter, SceneObject obj) {
        return inter.getNormal();
    }

    @Override
    public boolean isInside(Vec3 point) {
        return objA.isInside(point) || objB.isInside(point);
    }

    @Override
    public UnionObject transform(Mat4 transformMatrix) {
        return new UnionObject(objA.transform(transformMatrix), objB.transform(transformMatrix), getMaterial());
    }
}