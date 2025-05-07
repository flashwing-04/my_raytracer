package stuff;

import math.*;

public class Color {

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

        //int rOut = (int) (r*255);
        //int gOut = (int) (g*255);
        //int bOut = (int) (b*255);


        return (rOut << 16) | (gOut << 8) | bOut;
    }

    public Vec3 getVector() {
        return new Vec3(r, g, b);
    }
}
