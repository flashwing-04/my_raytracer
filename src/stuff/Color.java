package stuff;

import math.*;

public class Color {

    public static final Color BLACK = new Color(0f, 0f, 0f);
    public static final Color WHITE = new Color(1f, 1f, 1f);

    private float r, g, b;

    public Color(float r, float g, float b) {
        //this.r = (float) Math.pow(clamp(r), 2.2);
        //this.g = (float) Math.pow(clamp(g), 2.2);
        //this.b = (float) Math.pow(clamp(b), 2.2);

        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    public Color(Vec3 colors) {
        this(colors.getX(), colors.getY(), colors.getZ());
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
        int rOut = (int)(Math.pow(clamp(r), 1.0 / 2.2) * 255);
        int gOut = (int)(Math.pow(clamp(g), 1.0 / 2.2) * 255);
        int bOut = (int)(Math.pow(clamp(b), 1.0 / 2.2) * 255);

        return (rOut << 16) | (gOut << 8) | bOut;
    }

    public Vec3 getVector() {
        return new Vec3(r, g, b);
    }

    public boolean similar(Color other, float threshold) {
        Vec3 v1 = this.getVector();
        Vec3 v2 = other.getVector();
        return v1.subtract(v2).getLength() < threshold;
    }
}
