package stuff;

public class Material {

    private Color albedo;
    private float roughness;
    private float transparency;

    public Material(Color albedo, float roughness, float transparency) {
        this.albedo = albedo;
        this.roughness = roughness;
        this.transparency = transparency;
    }

    public Color getAlbedo() {
        return albedo;
    }

    public float getRoughness() {
        return roughness;
    }

    public float getTransparency() {
        return transparency;
    }
}
