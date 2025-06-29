package stuff;

import math.Vec3;

/**
 * Represents the physical material properties for rendering.
 * Encapsulates parameters such as albedo color, roughness, metalness,
 * transmission, index of refraction (ior), and the Fresnel reflectance at normal incidence (F0).
 */
public class Material {

    private final Color albedo;
    private final float roughness, metalness, transmission, ior;
    private final Vec3 F0;

    /**
     * Constructs a Material with the specified properties.
     * Calculates the Fresnel reflectance vector F0 based on albedo and metalness.
     *
     * @param albedo Base color of the material
     * @param roughness Surface roughness (0 to 1)
     * @param metalness Metalness factor (0 to 1)
     * @param transmission Transmission factor (0 to 1)
     * @param ior Index of refraction for transparent materials
     */
    public Material(Color albedo, float roughness, float metalness, float transmission, float ior) {
        this.albedo = albedo;
        this.roughness = roughness;
        this.metalness = metalness;
        this.transmission = transmission;
        this.ior = ior;
        this.F0 = calculateF0(this);
    }

    /**
     * Returns the albedo (base color) of the material.
     *
     * @return The albedo color
     */
    public Color getAlbedo() {
        return albedo;
    }

    /**
     * Returns the roughness value of the material.
     *
     * @return Roughness in the range [0,1]
     */
    public float getRoughness() {
        return roughness;
    }

    /**
     * Returns the metalness factor of the material.
     *
     * @return Metalness in the range [0,1]
     */
    public float getMetalness() {
        return metalness;
    }

    /**
     * Returns the transmission factor indicating material transparency.
     *
     * @return Transmission in the range [0,1]
     */
    public float getTransmission() {
        return transmission;
    }

    /**
     * Returns the index of refraction (IOR) of the material.
     *
     * @return IOR value
     */
    public float getIor() {
        return ior;
    }

    /**
     * Returns the Fresnel reflectance at normal incidence vector (F0).
     * This is used in shading calculations.
     *
     * @return Fresnel reflectance vector
     */
    public Vec3 getF0() {
        return F0;
    }

    /**
     * Calculates the Fresnel reflectance at normal incidence (F0) for the material.
     *
     * For dielectric materials (non-metals), F0 is typically around 0.04.
     * For metals, F0 is mixed with the material's albedo color weighted by metalness.
     *
     * @param material The material to calculate F0 for
     * @return The Fresnel reflectance vector (Vec3)
     */
    private Vec3 calculateF0(Material material) {
        Vec3 F0 = new Vec3(0.04f);  // Default for dielectric materials
        // Linearly interpolate between dielectric F0 and albedo based on metalness
        return F0.mix(material.getAlbedo().getVector(), new Vec3(material.getMetalness()));
    }
}