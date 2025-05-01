package lighting.models;

import java.util.ArrayList;

import math.geometry.objects.*;
import math.geometry.*;
import lighting.*;
import math.Vec3;
import stuff.*;

public abstract class LightingModel {

    ArrayList<Light> lights;
    SceneObject object;
    Intersection intersection;

    public LightingModel(ArrayList<Light> lights, SceneObject object, Intersection intersection) {
        this.lights = lights;
        this.object = object;
        this.intersection = intersection;
    }

    protected abstract Vec3 computeLight();

    public int getFinalColor() { return new Color(computeLight()).toHex(); }

    public ArrayList<Light> getLights() {
        return lights;
    }

    public SceneObject getObject() {
        return object;
    }

    public Intersection getIntersection() {
        return intersection;
    }
}

