package lighting.models;

import lighting.*;
import math.Vec3;
import stuff.Material;

public class LambertLighting extends LightingModel {

    @Override
    public Vec3 computeLight(LightingContext ctx) {
        Vec3 lighting = Vec3.ZERO;
        Vec3 point = ctx.intersection.getPoint();
        Vec3 normal = ctx.intersection.getNormal();
        Material material = ctx.object.getMaterial();
        Vec3 albedo = material.getAlbedo().getVector();

        for (Light light : ctx.lights) {
            Vec3 lightDir = light.getP().subtract(point).normalize();
            float dot = normal.dot(lightDir);

            if (dot > 0) {
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

        return lighting.multiply(albedo).add(ctx.ambient);
    }
}