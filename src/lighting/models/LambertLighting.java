package lighting.models;

import lighting.*;
import math.Vec3;
import stuff.Material;

/**
 * LambertLighting implements the Lambertian diffuse lighting model.
 *
 * It computes the diffuse light contribution from all scene lights
 * based on the Lambert cosine law.
 *
 * The final color is the sum of all light contributions scaled by the
 * surface albedo and ambient lighting.
 */
public class LambertLighting extends LightingModel {

    /**
     * Computes the Lambert diffuse lighting contribution at the intersection point.
     *
     * @param ctx the lighting context containing intersection, lights, ambient light, etc.
     * @return the computed color as a Vec3 representing RGB intensity.
     */
    public Vec3 computeLight(LightingContext ctx) {
        Vec3 lighting = Vec3.ZERO;
        Vec3 point = ctx.intersection().point();
        Vec3 normal = ctx.intersection().normal();
        Material material = ctx.intersection().material();
        Vec3 albedo = material.getAlbedo().getVector();

        for (Light light : ctx.lights()) {
            Vec3 lightDir = light.getP().subtract(point).normalize();
            float dot = normal.dot(lightDir);

            if (dot > 0) {
                float attenuation = 1.0f;

                // Apply attenuation for spotlights (skip light if fully attenuated)
                if (light instanceof SpotLight spot) {
                    attenuation = spot.getAttenuation(point);
                    if (attenuation <= 0f) continue;
                }

                Vec3 lightColor = light.getColor().getVector().multiply(light.getIntensity() * attenuation);
                Vec3 lightContribution = lightColor.multiply(dot);

                lighting = lighting.add(lightContribution);
            }
        }

        // Multiply total light by surface albedo and add ambient light
        return lighting.multiply(albedo).add(ctx.ambient());
    }
}