package lighting.models;

import lighting.Light;
import lighting.LightingContext;
import lighting.SpotLight;
import math.Vec3;
import stuff.Material;


public class CookTorranceLighting extends LightingModel {

    public Vec3 computeLight(LightingContext ctx) {
        Vec3 point = ctx.intersection.getPoint();
        Vec3 normal = ctx.intersection.getNormal();
        Vec3 view = ctx.camera.getPosition().subtract(point).normalize();

        Material material = ctx.object.getMaterial();
        Vec3 albedo = material.getAlbedo().getVector();
        float roughness = material.getRoughness();
        float metalness = material.getMetalness();

        Vec3 finalColor = Vec3.ZERO;

        float nv = Math.max(normal.dot(view), 0.0f);
        Vec3 F = fresnelSchlick(nv, material);

        for( Light light : ctx.lights) {
            Vec3 lightDir = (light.getP().subtract(point)).normalize();
            Vec3 h = view.add(lightDir).normalize();

            float nh = Math.max(normal.dot(h), 0.0f);
            float nl = Math.max(normal.dot(lightDir), 0.0f);

            float D = distributionGGX(nh, roughness);
            float G = geometrySmith(nv, nl, roughness);

            Vec3 kSpecular = (F.multiply(D*G)).multiply(1.0f / (4.0f * nv * nl + 0.0001f));

            Vec3 kDiffuse = (new Vec3(1).subtract(kSpecular)).multiply(1.0f - metalness);

            float attenuation = 1f;
            if (light instanceof SpotLight spot) {
                attenuation = spot.getAttenuation(point);
            }

            Vec3 contribution = (light.getColor().getVector().multiply(light.getIntensity() * nl * attenuation)).multiply(kDiffuse.multiply(albedo).add(kSpecular));
            finalColor = finalColor.add(contribution);
        }

        finalColor = finalColor.add(ctx.ambient);
        return finalColor;
    }

    private float distributionGGX(float nh, float roughness){
        float r2 = roughness * roughness;
        float nh2 = nh * nh;

        float denominator = (nh2 * (r2 - 1.0f) + 1.0f);
        denominator = ((float) Math.PI) * denominator * denominator;

        return r2 / denominator;
    }

    private Vec3 fresnelSchlick(float cosTheta, Material material) {
        Vec3 F0 = material.getF0();
        float factor = (float) Math.pow(Math.max(0.0, Math.min(1.0, 1.0 - cosTheta)), 5.0);
        return F0.add(((new Vec3(1).subtract(F0)).multiply(factor)));
    }

    float geometrySmith(float nv, float nl, float roughness) {
        float ggx1 = geometrySchlickGGX(nv, roughness);
        float ggx2 = geometrySchlickGGX(nl, roughness);

        return ggx1 * ggx2;
    }

    float geometrySchlickGGX(float nv, float roughness) {
        float k = roughness/2.0f;
        float denominator = nv * (1.0f - k) + k;

        return nv/ denominator;
    }
}
