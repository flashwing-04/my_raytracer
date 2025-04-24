package lighting.models;

import java.util.ArrayList;

import geometry.objects.*;
import geometry.*;
import lighting.*;
import stuff.*;

public abstract class LightingModel {

    ArrayList<Light> lights;
    SceneObject object;
    Intersection sp;

    public LightingModel(ArrayList<Light> lights, SceneObject object, Intersection sp) {
        this.lights = lights;
        this.object = object;
        this.sp = sp;
    }

    protected abstract Vec3 computeLight();

    public int computeFinalColor() {
        Vec3 lighting = computeLight();

        float red = lighting.getX() * object.getMaterial().getAlbedo().getR();
        float green = lighting.getY() * object.getMaterial().getAlbedo().getG();
        float blue = lighting.getZ() * object.getMaterial().getAlbedo().getB();

        return new Color(red, green, blue).toHex();
    }

    public ArrayList<Light> getLights() {
        return lights;
    }

    public SceneObject getObject() {
        return object;
    }

    public Intersection getSp() {
        return sp;
    }
}

