package geometry.objects;

import geometry.*;
import stuff.*;

public abstract class SceneObject {

    private Material material;

    public SceneObject(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public abstract float computeIntersectionS(Ray ray);

    public abstract Vec3 getNormal(Vec3 p);

}
