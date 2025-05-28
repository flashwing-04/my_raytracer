package lighting;

import math.Vec3;
import math.geometry.Intersection;
import math.geometry.objects.SceneObject;
import scene.Camera;

import java.util.List;

public class LightingContext {
    public final List<Light> lights;
    public final SceneObject object;
    public final Intersection intersection;
    public final Camera camera;
    public final Vec3 ambient;
    public final float currentIor;

    public LightingContext(List<Light> lights, SceneObject object, Intersection intersection, Camera camera, Vec3 ambient, float currentIor) {
        this.lights = lights;
        this.object = object;
        this.intersection = intersection;
        this.camera = camera;
        this.ambient = ambient;
        this.currentIor = currentIor;
    }
}
