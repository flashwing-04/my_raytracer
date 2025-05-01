package stuff;

public class Material {

    private Color albedo;
    private float roughness;
    private float metalness;
    private float transparency;

    public Material(Color albedo, float roughness, float metalness, float transparency) {
        this.albedo = albedo;
        this.roughness = roughness;
        this.metalness = metalness;
        this.transparency = transparency;
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
}
