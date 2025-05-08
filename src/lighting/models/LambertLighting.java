package lighting.models;

import java.util.ArrayList;

import math.*;
import math.geometry.objects.*;
import math.geometry.*;
import lighting.*;

public class LambertLighting extends LightingModel {

    public LambertLighting(ArrayList<Light> lights, SceneObject object, Intersection intersection) {
        super(lights, object, intersection);
    }

    public Vec3 computeLight() {
        Vec3 lighting = new Vec3(0);
        Vec3 point = intersection.getPoint();
        Vec3 normal = intersection.getNormal();

        for( Light light : lights) {
            Vec3 lightDir = (light.getP().subtract(point)).normalize();
            float dot = normal.dot(lightDir);

            if(dot > 0) {
                float attenuation = 1.0f;

                if (light instanceof SpotLight spot) {
                    attenuation = spot.getAttenuation(point);
                    if (attenuation <= 0f) continue;
                }

                Vec3 lightColor = light.getColor().getVector().multiply(light.getIntensity() * attenuation);
                Vec3 lightContribution = lightColor.multiply(dot);

                lighting = lighting.add(lightContribution);
            }
        }

        return lighting.multiply(object.getMaterial().getAlbedo().getVector());
    }
}
