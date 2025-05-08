package lighting.models;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import lighting.*;
import scene.*;
import stuff.*;

import java.sql.SQLOutput;
import java.util.*;

public class CookTorranceLighting extends LightingModel {

    private Camera camera;
    private Vec3 ambient;

    public CookTorranceLighting(ArrayList<Light> lights, SceneObject object, Intersection intersection, Camera camera, Vec3 ambient) {
        super(lights, object, intersection);
        this.camera = camera;
        this.ambient = ambient;
    }

    public Vec3 computeLight() {
        Vec3 point = intersection.getPoint();
        Vec3 normal = intersection.getNormal();
        Vec3 view = camera.getPosition().subtract(point).normalize();

        Material material = object.getMaterial();
        Vec3 albedo = material.getAlbedo().getVector();
        float roughness = material.getRoughness();
        float metalness = material.getMetalness();
        
        Vec3 finalColor = new Vec3(0);

        float nv = Math.max(normal.dot(view), 0.0f);
        Vec3 F = fresnelSchlick(nv, material);

        for( Light light : lights) {
            Vec3 lightDir = (light.getP().subtract(point)).normalize();
            Vec3 h = view.add(lightDir).normalize();

            float nh = Math.max(normal.dot(h), 0.0f);
            float nl = Math.max(normal.dot(lightDir), 0.0f);

            float D = distributionGGX(nh, roughness);
            float G = geometrySmith(nv, nl, roughness);

            Vec3 kSpecular = (F.multiply(D*G)).multiply(1.0f / (4.0f * nv * nl + 0.0001f));

            Vec3 kDiffuse = (new Vec3(1, 1, 1).subtract(kSpecular)).multiply(1.0f - metalness);

            float attenuation = 1f;
            if (light instanceof SpotLight spot) {
                attenuation = spot.getAttenuation(point);
            }

            Vec3 contribution = (light.getColor().getVector().multiply(light.getIntensity() * nl * attenuation)).multiply(kDiffuse.multiply(albedo).add(kSpecular));
            finalColor = finalColor.add(contribution);
        }

        finalColor = mix(finalColor, getReflection(material, view, normal), new Vec3(metalness));
        finalColor = finalColor.add(ambient);

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
        Vec3 F0 = calculateF0(material);
        float factor = (float) Math.pow(Math.max(0.0, Math.min(1.0, 1.0 - cosTheta)), 5.0);
        return F0.add(((new Vec3(1, 1, 1).subtract(F0)).multiply(factor)));
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

    private Vec3 getReflection(Material material, Vec3 view, Vec3 normal) {
        //Vec3 reflection = new Vec3(0.6f);     //for now, reflect gray
        Vec3 reflection = new Vec3(1);       //for now, reflect white
        Vec3 F = fresnelSchlick((float) Math.max(view.dot(normal), 0.0), material);

        return mix(new Vec3(0), reflection, F);
    }

    private Vec3 calculateF0(Material material) {
        Vec3 F0 = new Vec3(0.04f);          // default F0 for dielectric materials
        return mix(F0, material.getAlbedo().getVector(), new Vec3(material.getMetalness()));
    }

    private Vec3 mix(Vec3 x, Vec3 y, Vec3 a) {
        return new Vec3(
                x.getX() * (1 - a.getX()) + y.getX() * a.getX(),
                x.getY() * (1 - a.getY()) + y.getY() * a.getY(),
                x.getZ() * (1 - a.getZ()) + y.getZ() * a.getZ()
        );
    }
}
