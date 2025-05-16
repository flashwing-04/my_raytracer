import java.awt.*;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.*;
import java.util.List;
import javax.swing.*;

import lighting.*;
import lighting.models.*;
import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import math.geometry.objects.csg.*;
import scene.*;
import stuff.*;
import stuff.Color;

public class MyRaytracer {

    private static final int RES_X = 1024;
    private static final int RES_Y = 1024;
    private static final int[] pixels = new int[RES_X * RES_Y];

    private static final int SOFT_SHADOW_SAMPLES = 1;
    private static final float LIGHT_RADIUS = 0f;

    private static final float EPSILON = 1e-4f;
    private static MemoryImageSource imageSource;

    private static final LightingModel cookTorranceLighting = new CookTorranceLighting();

    public static void main(String[] args) {
        setUpWindow();

        Vec3 cameraPos = new Vec3(0, 0, 0);
        Vec3 cameraV = new Vec3(0, 0, -1);
        Vec3 imgPlaneR = new Vec3(1, 0, 0);
        Camera camera = new Camera(cameraPos,cameraV, imgPlaneR, 2, 2, 1);

        List<SceneObject> sceneObjects = getCSG();
        List<Light> lights = getLights();

        renderScene(camera, sceneObjects, lights);
        imageSource.newPixels();
    }

    private static void renderScene(Camera camera, List<SceneObject> objects, List<Light> lights) {
        Vec3 pxStart = camera.getPxStart();
        Vec3 stepRight = camera.getPxRightStep(RES_X);
        Vec3 stepUp = camera.getPxUpStep(RES_Y);

        for (int y = 0; y < RES_Y; ++y) {
            for (int x = 0; x < RES_X; ++x) {
                Vec3 pixelPos = pxStart.add(stepRight.multiply(x)).add(stepUp.multiply(y));
                Ray ray = new Ray(camera.getPosition(), pixelPos.subtract(camera.getPosition()));
                Color color = traceRay(ray, objects, lights, camera, 5);
                pixels[y * RES_X + x] = color.toHex();
            }
            imageSource.newPixels();
        }
    }

    private static Color traceRay(Ray ray, List<SceneObject> objects, List<Light> lights, Camera camera, int depth) {
        if (depth <= 0) return Color.BLACK;

        Intersection nearestIntersection = getNearestIntersection(ray, objects);
        if (nearestIntersection == null) return Color.BLACK;

        SceneObject hitObject = nearestIntersection.getObject();
        Material material = hitObject.getMaterial();

        List<Light> relevantLights = computeSoftShadows(nearestIntersection, lights, objects);

        LightingContext context = new LightingContext(relevantLights, hitObject, nearestIntersection, camera, Vec3.ZERO);
        Color localColor = cookTorranceLighting.getFinalColor(context);


        Vec3 viewDir = ray.getV().multiply(-1).normalize();
        float cosTheta = Math.max(0f, nearestIntersection.getNormal().dot(viewDir));
        float fresnel = material.getF0().getX() + (1 - material.getF0().getX()) * (float)Math.pow(1 - cosTheta, 5);
        float reflectivity = fresnel * (1 - material.getRoughness());

        if (reflectivity >= 0.01f) {
            Vec3 reflectionDir = ray.getV().reflect(nearestIntersection.getNormal());
            Vec3 reflectionOrigin = nearestIntersection.getPoint().add(reflectionDir.multiply(EPSILON));
            Color reflected = traceRay(new Ray(reflectionOrigin, reflectionDir), objects, lights, camera, depth - 1);
            Vec3 blended = reflected.getVector().multiply(reflectivity).add(localColor.getVector().multiply(1 - reflectivity));
            return new Color(blended);
        }

        return localColor;
    }

    private static Intersection getNearestIntersection(Ray ray, List<SceneObject> objects) {
        Intersection nearestIntersection = null;
        float minDist = Float.MAX_VALUE;

        for (SceneObject obj : objects) {
            for (Intersection inter : obj.intersect(ray)) {
                float dist = inter.getDistance();
                if (dist > 0 && dist < minDist) {
                    minDist = dist;
                    nearestIntersection = inter;
                }
            }
        }
        return nearestIntersection;
    }

    private static List<Light> computeSoftShadows(Intersection hit, List<Light> lights, List<SceneObject> objects) {
        List<Light> relevantLights = new ArrayList<>();
        Vec3 point = hit.getPoint();

        for (Light light : lights) {
            float shadowCount = 0;

            for (int i = 0; i < SOFT_SHADOW_SAMPLES; i++) {
                Vec3 samplePos = jitterLightPosition(light);
                Vec3 toLight = samplePos.subtract(point);
                float distance = toLight.getLength();

                Ray shadowRay = new Ray(point.add(toLight.normalize().multiply(EPSILON)), toLight);
                boolean occluded = objects.stream().anyMatch(obj -> obj.isOccluding(shadowRay, distance));

                if (!occluded && light instanceof SpotLight spot && spot.getAttenuation(point) <= 0) {
                    occluded = true;
                }

                if (!occluded) shadowCount += 1;
            }

            float factor = shadowCount / SOFT_SHADOW_SAMPLES;
            if (factor > 0) {
                relevantLights.add(light.copyWithIntensity(light.getIntensity() * factor));
            }
        }

        return relevantLights;
    }

    private static Vec3 jitterLightPosition(Light light) {
        Vec3 disk = randomPointInDisk();
        if (light instanceof SpotLight spot) {
            Vec3 dir = spot.getDirection();
            Vec3 up = Math.abs(dir.getY()) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
            Vec3 right = up.cross(dir).normalize();
            Vec3 localUp = dir.cross(right).normalize();
            return light.getP().add(right.multiply(disk.getX())).add(localUp.multiply(disk.getY()));
        }
        return light.getP().add(disk);
    }

    private static Vec3 randomPointInDisk() {
        double r = MyRaytracer.LIGHT_RADIUS * Math.sqrt(Math.random());
        double theta = 2 * Math.PI * Math.random();
        return new Vec3((float)(r * Math.cos(theta)), (float)(r * Math.sin(theta)), 0f);
    }

    private static void setUpWindow() {
        imageSource = new MemoryImageSource(RES_X, RES_Y, new DirectColorModel(24, 0xff0000, 0xff00, 0xff), pixels, 0, RES_X);
        imageSource.setAnimated(true);
        Image image = Toolkit.getDefaultToolkit().createImage(imageSource);

        JFrame frame = new JFrame("My Raytracer");
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static List<Light> getLights() {
        return Arrays.asList(
                new SpotLight(new Vec3(0, 0, 3), 1f, Color.WHITE, new Vec3(0, 0, -1), (float) Math.toRadians(15), 1f),
                new Light(new Vec3(1, 1, 2), 1f, Color.WHITE),
                new Light(new Vec3(-3, 0, 0), 1f, Color.WHITE)
        );
    }

    private static List<SceneObject> getCSG() {
        List<SceneObject> objects = new ArrayList<>();

        Material redish = new Material(new Color(0.5f, 0.2f, 0.3f), 0.9f, 0.01f, 0, 1);
        Material greenish = new Material(new Color(0.4f, 0.9f, 0.6f), 0.2f, 0.7f, 0, 1);

        objects.add(new Area(new Vec3(0, 1, 0), -1, redish));

        Mat4 transform = new Mat4().rotateY(0.5f).translate(0, 0, -3);
        SceneObject baseSphere = new Quadrik(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.6f}, redish);
        SceneObject cube = makeCube(redish);
        SceneObject cylX = new Quadrik(new float[]{1, 1, 0, 0, 0, 0, 0, 0, 0, -0.2f}, redish);
        SceneObject cylY = new Quadrik(new float[]{0, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f}, redish);

        SceneObject csgShape = new DifferenceObject(new DifferenceObject(new IntersectionObject(baseSphere, cube, redish), cylX, redish), cylY, redish).transform(transform);

        SceneObject greenSphere = new Quadrik(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f}, greenish)
                .transform(transform);

        objects.add(csgShape);
        objects.add(greenSphere);

        return objects;
    }

    private static SceneObject makeCube(Material material) {
        List<SceneObject> faces = Arrays.asList(
                new Quadrik(new float[]{0, 0, 0, 0, 0, 0, -1, 0, 0, -1}, material),
                new Quadrik(new float[]{0, 0, 0, 0, 0, 0, 1, 0, 0, -1}, material),
                new Quadrik(new float[]{0, 0, 0, 0, 0, 0, 0, -1, 0, -1}, material),
                new Quadrik(new float[]{0, 0, 0, 0, 0, 0, 0, 1, 0, -1}, material),
                new Quadrik(new float[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1}, material),
                new Quadrik(new float[]{0, 0, 0, 0, 0, 0, 0, 0, 1, -1}, material)
        );

        return faces.stream().reduce((a, b) -> new IntersectionObject(a, b, material)).orElseThrow();
    }
}
