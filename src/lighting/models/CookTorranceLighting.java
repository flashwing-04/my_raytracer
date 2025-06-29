package lighting.models;

import lighting.*;
import math.Vec3;
import stuff.Material;

/**
 * Implements the Cook-Torrance physically based lighting model.
 * Computes the color contribution from lights using GGX microfacet distribution,
 * Smith geometry term, and Fresnel equations with optional metalness and roughness.
 */
public class CookTorranceLighting extends LightingModel {

    /**
     * Computes the final lighting color at the intersection point.
     *
     * @param ctx The lighting context containing intersection, lights, camera, etc.
     * @return The computed color as a Vec3.
     */
    public Vec3 computeLight(LightingContext ctx) {
        Vec3 point = ctx.intersection().point();
        Vec3 normal = ctx.intersection().normal();
        Vec3 view = ctx.camera().getPosition().subtract(point).normalize();

        Material material = ctx.intersection().material();
        Vec3 albedo = material.getAlbedo().getVector();
        float roughness = material.getRoughness();
        float metalness = material.getMetalness();

        // Determine if ray is entering or exiting the surface for IOR calculation
        boolean entering = -view.dot(normal) > 0;
        float iorFrom = entering ? ctx.currentIor() : material.getIor();
        float iorTo = entering ? material.getIor() : ctx.currentIor();
        Vec3 refractionNormal = entering ? normal : normal.multiply(-1);

        Vec3 finalColor = Vec3.ZERO;
        float nv = Math.max(normal.dot(view), 0f);

        // Fresnel reflectance at view angle
        Vec3 F = new Vec3(calculateFresnel(view, refractionNormal, iorFrom, iorTo));

        for (Light light : ctx.lights()) {
            Vec3 lightDir = light.getP().subtract(point).normalize();
            float nl = Math.max(normal.dot(lightDir), 0f);
            if (nl <= 0) continue; // light facing away

            Vec3 h = view.add(lightDir).normalize();
            float nh = Math.max(normal.dot(h), 0f);

            float D = distributionGGX(nh, roughness);
            float G = geometrySmith(nv, nl, roughness);
            float denom = 4f * nv * nl + 1e-4f;

            Vec3 specular = F.multiply(D * G / denom);

            // Diffuse component scaled by non-metal portion
            Vec3 diffuse = Vec3.ONE.subtract(specular).multiply(1f - metalness);

            float attenuation = 1f;
            if (light instanceof SpotLight spot) {
                attenuation = spot.getAttenuation(point);
                if (attenuation <= 0f) continue;
            }

            Vec3 radiance = light.getColor().getVector().multiply(light.getIntensity() * nl * attenuation);

            Vec3 contribution = radiance.multiply(diffuse.multiply(albedo).add(specular));
            finalColor = finalColor.add(contribution);
        }

        // Add ambient lighting term from context
        return finalColor.add(ctx.ambient());
    }

    /**
     * GGX / Trowbridge-Reitz normal distribution function.
     *
     * @param nh       Cosine of angle between normal and half-vector.
     * @param roughness Surface roughness.
     * @return Distribution term D.
     */
    public float distributionGGX(float nh, float roughness) {
        float r2 = roughness * roughness;
        float denom = (nh * nh) * (r2 - 1f) + 1f;
        denom = (float) Math.PI * denom * denom;
        return r2 / denom;
    }

    /**
     * Calculates Fresnel reflectance using exact equations for dielectric interface.
     *
     * @param viewDir Incident view direction (normalized).
     * @param normal  Surface normal (oriented towards incident medium).
     * @param IorFrom Index of refraction of incident medium.
     * @param IorTo   Index of refraction of transmitted medium.
     * @return Fresnel reflectance coefficient between 0 and 1.
     */
    public float calculateFresnel(Vec3 viewDir, Vec3 normal, float IorFrom, float IorTo) {
        float cosThetaI = -viewDir.dot(normal);
        cosThetaI = Math.min(1f, Math.max(-1f, cosThetaI));

        float eta = IorFrom / IorTo;
        float sinThetaTSq = eta * eta * (1f - cosThetaI * cosThetaI);

        if (sinThetaTSq > 1f) {
            return 1f;  // Total internal reflection
        }

        float cosThetaT = (float) Math.sqrt(1f - sinThetaTSq);

        float Rs = ((IorFrom * cosThetaI) - (IorTo * cosThetaT)) / ((IorFrom * cosThetaI) + (IorTo * cosThetaT));
        Rs *= Rs;

        float Rp = ((IorTo * cosThetaI) - (IorFrom * cosThetaT)) / ((IorTo * cosThetaI) + (IorFrom * cosThetaT));
        Rp *= Rp;

        return (Rs + Rp) * 0.5f;
    }

    /**
     * Smith geometry term for microfacet shadowing-masking.
     *
     * @param nv        Cosine of angle between normal and view direction.
     * @param nl        Cosine of angle between normal and light direction.
     * @param roughness Surface roughness.
     * @return Geometry attenuation factor.
     */
    float geometrySmith(float nv, float nl, float roughness) {
        return geometrySchlickGGX(nv, roughness) * geometrySchlickGGX(nl, roughness);
    }

    /**
     * Schlick-GGX geometry attenuation term for a single direction.
     *
     * @param nDotV     Cosine between normal and vector (view or light).
     * @param roughness Surface roughness.
     * @return Geometry term for one direction.
     */
    float geometrySchlickGGX(float nDotV, float roughness) {
        float k = (roughness * roughness) / 2f;
        return nDotV / (nDotV * (1f - k) + k);
    }

}