package lighting.models;

import lighting.LightingContext;
import math.Vec3;
import stuff.Color;

/**
 * Abstract base class for all lighting models.
 *
 * Provides a contract to compute lighting as a Vec3 and
 * a helper to convert the computed lighting to a Color.
 */
public abstract class LightingModel {

    /**
     * Computes the lighting contribution given the lighting context.
     *
     * @param ctx the lighting context containing scene, intersection, lights, etc.
     * @return the computed lighting as a Vec3 representing RGB intensity.
     */
    public abstract Vec3 computeLight(LightingContext ctx);

    /**
     * Convenience method to get the final color after lighting calculation.
     *
     * Converts the Vec3 lighting result into a Color object.
     *
     * @param ctx the lighting context
     * @return the computed color as a Color instance
     */
    public Color getFinalColor(LightingContext ctx) {
        return new Color(computeLight(ctx));
    }
}