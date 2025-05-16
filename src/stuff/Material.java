package stuff;

import math.Vec3;

public class Material {

    private Color albedo;
    private float roughness;
    private float metalness;
    private float transparency;
    private float ior;
    private Vec3 F0;

    public Material(Color albedo, float roughness, float metalness, float transparency, float ior) {
        this.albedo = albedo;
        this.roughness = roughness;
        this.metalness = metalness;
        this.transparency = transparency;
        this.ior = ior;
        this.F0 = calculateF0(this);
    }

    public Color getAlbedo() {
        return albedo;
    }

    public float getRoughness() {
        return roughness;
    }

    public float getMetalness() { return metalness; }

    public float getTransparency() {
        return transparency;
    }

    public float getIor() {
        return ior;
    }

    public Vec3 getF0() {
        return F0;
    }

    private Vec3 calculateF0(Material material) {
        Vec3 F0 = new Vec3(0.04f);          // default F0 for dielectric materials
        return F0.mix(material.getAlbedo().getVector(), new Vec3(material.getMetalness()));
    }

}
