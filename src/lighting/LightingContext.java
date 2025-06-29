package lighting;

import math.Vec3;
import math.geometry.Intersection;
import math.geometry.objects.SceneObject;
import scene.Camera;

import java.util.List;

/**
 * Encapsulates the context required for lighting calculations at a specific point in the scene.
 *
 * Provides information about the lights, intersected object and point, the camera,
 * ambient lighting contribution, and the current index of refraction (IOR) for accurate
 * physically based rendering.
 *
 *
 * @param lights      List of lights influencing the scene at the intersection.
 * @param object      The scene object that was intersected by a ray.
 * @param intersection Details of the intersection point on the object (position, normal, material, etc.).
 * @param camera      The camera viewing the scene.
 * @param ambient     Ambient light color contribution as a vector.
 * @param currentIor  The current index of refraction in the medium where the ray is located.
 */
public record LightingContext(
        List<Light> lights,
        SceneObject object,
        Intersection intersection,
        Camera camera,
        Vec3 ambient,
        float currentIor) {
}