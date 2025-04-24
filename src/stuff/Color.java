package stuff;

import geometry.Vec3;

public class Color {

    private float r, g, b;

    public Color(float r, float g, float b) {
        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    public Color(Vec3 colors) {
        this.r = clamp(colors.getX());
        this.g = clamp(colors.getY());
        this.b = clamp(colors.getZ());
    }

    private float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public int toHex() {
        return ((int)(r*255) << 16) | ( (int)(g*255) << 8) | (int)(b*255);
    }

    public Vec3 getVector() {
        return new Vec3(r, g, b);
    }
}
