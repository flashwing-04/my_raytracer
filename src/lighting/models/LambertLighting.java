package lighting.models;

import java.util.ArrayList;

import geometry.objects.*;
import geometry.*;
import lighting.*;

public class LambertLighting extends LightingModel {

    public LambertLighting(ArrayList<Light> lights, SceneObject object, Intersection sp) {
        super(lights, object, sp);
    }

    public Vec3 computeLight() {
        Vec3 lighting = new Vec3(0, 0, 0);
        Vec3 point = sp.getPoint();
        Vec3 normal = sp.getNormal();

        for( Light light : lights) {
            Vec3 lightDir = (light.getP().subtract(point)).normalize();
            float dot = normal.dot(lightDir);

            if(dot > 0) {
                Vec3 lightColor = light.getColor().getVector().multiply(light.getIntensity());
                Vec3 lightContribution = lightColor.multiply(dot);

                lighting = lighting.add(lightContribution);
            }
        }
        return lighting;
    }
}
