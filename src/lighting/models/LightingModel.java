package lighting.models;

import lighting.LightingContext;
import math.Vec3;
import stuff.Color;

public abstract class LightingModel {
  
    public abstract Vec3 computeLight(LightingContext ctx);
  
    public Color getFinalColor(LightingContext ctx) {
        return new Color(computeLight(ctx));
}
