package stuff;

import math.*;

/**
 * Represents an RGB color with components in the range [0, 1].
 * Provides utility methods for color manipulation, conversion, and comparison.
 */
public class Color {

    public static final Color BLACK = new Color(0f, 0f, 0f);
    public static final Color WHITE = new Color(1f, 1f, 1f);

    private final float r, g, b;

    /**
     * Constructs a Color with specified red, green, and blue components.
     * Each component is clamped to the range [0, 1].
     *
     * @param r Red component (0 to 1)
     * @param g Green component (0 to 1)
     * @param b Blue component (0 to 1)
     */
    public Color(float r, float g, float b) {
        // Gamma correction was commented out, currently stores linear color values.
        // this.r = (float) Math.pow(clamp(r), 2.2);
        // this.g = (float) Math.pow(clamp(g), 2.2);
        // this.b = (float) Math.pow(clamp(b), 2.2);

        this.r = clamp(r);
        this.g = clamp(g);
        this.b = clamp(b);
    }

    /**
     * Constructs a Color from a {@link Vec3} representing RGB components.
     * The components of the vector are used as (r, g, b).
     *
     * @param colors Vector containing RGB components
     */
    public Color(Vec3 colors) {
        this(colors.getX(), colors.getY(), colors.getZ());
    }

    /**
     * Clamps a float value to the range [0, 1].
     *
     * @param value The value to clamp
     * @return The clamped value between 0 and 1
     */
    private float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    /**
     * Gets the red component of this color.
     *
     * @return Red component in [0, 1]
     */
    public float getR() {
        return r;
    }

    /**
     * Gets the green component of this color.
     *
     * @return Green component in [0, 1]
     */
    public float getG() {
        return g;
    }

    /**
     * Gets the blue component of this color.
     *
     * @return Blue component in [0, 1]
     */
    public float getB() {
        return b;
    }

    /**
     * Converts this color to an integer hex representation (0xRRGGBB).
     * Applies gamma correction with gamma = 2.2 before conversion.
     *
     * @return Integer color in 0xRRGGBB format
     */
    public int toHex() {
        int rOut = (int)(Math.pow(clamp(r), 1.0 / 2.2) * 255);
        int gOut = (int)(Math.pow(clamp(g), 1.0 / 2.2) * 255);
        int bOut = (int)(Math.pow(clamp(b), 1.0 / 2.2) * 255);

        return (rOut << 16) | (gOut << 8) | bOut;
    }

    /**
     * Returns the RGB components as a {@link Vec3}.
     *
     * @return Vector containing (r, g, b)
     */
    public Vec3 getVector() {
        return new Vec3(r, g, b);
    }

    /**
     * Adds another Color to this color component-wise and returns the result.
     *
     * @param other The other color to add
     * @return A new Color representing the sum
     */
    public Color add(Color other) {
        return new Color(this.getVector().add(other.getVector()));
    }

    /**
     * Multiplies this color by another color component-wise and returns the result.
     *
     * @param other The other color to multiply with
     * @return A new Color representing the component-wise multiplication
     */
    public Color multiply(Color other) {
        return new Color(this.r * other.getR(), this.g * other.getG(), this.b * other.getB());
    }

    /**
     * Determines if this color is similar to another color within a given threshold.
     * Similarity is based on Euclidean distance in RGB space.
     *
     * @param other The other color to compare against
     * @param threshold The maximum distance to be considered similar
     * @return True if the colors are within the threshold distance, false otherwise
     */
    public boolean similar(Color other, float threshold) {
        Vec3 v1 = this.getVector();
        Vec3 v2 = other.getVector();
        return v1.subtract(v2).getLength() < threshold;
    }
}