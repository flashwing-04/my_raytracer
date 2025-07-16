import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;

import lighting.*;
import lighting.models.*;
import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import scene.*;
import stuff.*;
import stuff.Color;

/**
 * A multi-threaded ray tracer rendering a 3D scene with support for
 * Cook-Torrance lighting, path tracing, soft shadows, reflections, refractions, and skybox environment.
 */
public class MyRaytracer {

    private static final int RES_X = 1024;
    private static final int RES_Y = 1024;
    private static final int[] pixels = new int[RES_X * RES_Y];

    private static final float EPSILON = 1e-3f;

    private static MemoryImageSource imageSource;

    private static CubeMap skybox = null;
    private static final CookTorranceLighting cookTorranceLighting = new CookTorranceLighting();
    private static final int DIFFUSE_REFLECTION_SAMPLES = 0;
    private static final int GLOSSY_REFLECTION_SAMPLES = 1;

    private static final int SOFT_SHADOW_SAMPLES =1;

    private static final int MAX_SUPERSAMPLING_DEPTH = 3;
    private static final int SUPERSAMPLING_SAMPLES_PER_SIDE = 1;
    private static final float COLOR_THRESHOLD = 0.05f;

    /**
     * Entry point: Sets up window, loads scene, skybox, and renders the scene.
     * Saves the final rendered image to disk.
     *
     * @param args command line arguments (ignored)
     * @throws IOException if loading skybox images fails
     */
    public static void main(String[] args) throws IOException {
        setUpWindow();

        // Setup camera position and orientation
        Vec3 cameraPos = new Vec3(0f, 0f, 0f);
        Vec3 cameraV = new Vec3(0, 0, -1);
        Vec3 imgPlaneR = new Vec3(1, 0, 0);
        Camera camera = new Camera(cameraPos, cameraV, imgPlaneR, 2, 2, 1);

        // Load scene parts
        List<SceneObject> sceneObjects = getCSG4(); // SceneBuilder.getObjects();
        List<Light> lights =getLights();    // SceneBuilder.getLights();
        skybox = SceneBuilder.getSkybox();

        System.out.println("Finished Setup");
        renderScene(camera, sceneObjects, lights);

        imageSource.newPixels();
        saveImageToFile();
    }

    /**
     * Renders the scene using multi-threading, launching a task for each image row.
     * Performs adaptive supersampling and ray tracing per pixel.
     *
     * @param camera the camera viewing the scene
     * @param objects list of scene objects to render
     * @param lights list of lights in the scene
     */
    private static void renderScene(Camera camera, List<SceneObject> objects, List<Light> lights) {
        Vec3 pxStart = camera.getPxStart();
        Vec3 stepRight = camera.getPxRightStep(RES_X);
        Vec3 stepUp = camera.getPxUpStep(RES_Y);

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        long startTime = System.nanoTime();

        final int rowsPerTask = Math.max(1, RES_Y / (threads * 4));

        for (int startRow = 0; startRow < RES_Y; startRow += rowsPerTask) {
            final int rowStart = startRow;
            final int rowEnd = Math.min(startRow + rowsPerTask, RES_Y);

            executor.submit(() -> {

                Stack<Float> initialIorStack = getInitialIorStack(camera.getPosition(), objects);

                for (int row = rowStart; row < rowEnd; row++) {
                    for (int x = 0; x < RES_X; ++x) {

                        Vec3 pixelTopLeft = pxStart.add(stepRight.multiply(x)).add(stepUp.multiply(row));

                        // Prepare index of refraction stack for handling nested transparent objects
                        Stack<Float> pixelIorStack = new Stack<>();
                        pixelIorStack.addAll(initialIorStack);

                        // Perform adaptive sampling to calculate pixel color
                        Color color = adaptiveSample(camera, pixelTopLeft, stepRight, stepUp, 0, objects, lights, pixelIorStack, 5);
                        pixels[row * RES_X + x] = color.toHex();
                    }
                }

                // Reduced synchronization frequency
                if (rowStart % (rowsPerTask * 4) == 0) {
                    synchronized (imageSource) {
                        imageSource.newPixels();
                    }
                    long elapsedTime = System.nanoTime() - startTime;
                    double seconds = elapsedTime / 1_000_000_000.0;
                    System.out.printf("Rendered rows %d-%d - Time elapsed: %.2f seconds%n", rowStart, rowEnd - 1, seconds);
                }
            });
        }

        // Shutdown executor and wait for completion of all tasks
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adaptives Supersampling N×N innerhalb eines Pixels mit einstellbarer Samples-Anzahl.
     * Teilt das Pixel nur weiter auf, wenn der Farbunterschied zwischen Subsamples zu groß ist.
     *
     * @param camera        Kamera
     * @param topLeft       Top-Left Position im Bildflächen-Raum für diesen Pixelbereich
     * @param stepX         Schritt-Vector für Pixelbreite
     * @param stepY         Schritt-Vector für Pixelhöhe
     * @param depth         aktuelle Rekursionstiefe der Supersampling-Unterteilung
     * @param objects       Szene-Objekte
     * @param lights        Lichter
     * @param iorStack      IOR Stack für Brechung
     * @param rayTraceDepth maximale Ray-Recursionstiefe für Trace-Aufrufe
     * @return Farbwert des Pixels
     */
    private static Color adaptiveSample(Camera camera, Vec3 topLeft, Vec3 stepX, Vec3 stepY, int depth, List<SceneObject> objects, List<Light> lights, Stack<Float> iorStack, int rayTraceDepth) {
        int totalSamples = SUPERSAMPLING_SAMPLES_PER_SIDE * SUPERSAMPLING_SAMPLES_PER_SIDE;
        Color[] colors = new Color[totalSamples];
        Vec3 cameraPos = camera.getPosition();

        int idx = 0;
        for (int y = 0; y < SUPERSAMPLING_SAMPLES_PER_SIDE; y++) {
            for (int x = 0; x < SUPERSAMPLING_SAMPLES_PER_SIDE; x++) {
                float fx = (x + 0.5f) / SUPERSAMPLING_SAMPLES_PER_SIDE;
                float fy = (y + 0.5f) / SUPERSAMPLING_SAMPLES_PER_SIDE;
                Vec3 samplePos = topLeft.add(stepX.multiply(fx)).add(stepY.multiply(fy));
                Ray ray = new Ray(cameraPos, samplePos.subtract(cameraPos));
                Stack<Float> localIorStack = new Stack<>();
                localIorStack.addAll(iorStack);
                colors[idx++] = traceRay(ray, objects, lights, camera, localIorStack, rayTraceDepth);
            }
        }

        boolean similar = true;
        outer:
        for (int i = 0; i < totalSamples && similar; i++) {
            for (int j = i + 1; j < totalSamples; j++) {
                if (!colors[i].similar(colors[j], COLOR_THRESHOLD)) {
                    similar = false;
                    break outer;
                }
            }
        }

        if (similar || depth >= MAX_SUPERSAMPLING_DEPTH) {
            Vec3 avg = Vec3.ZERO;
            for (Color c : colors) avg = avg.add(c.getVector());
            return new Color(avg.divide(totalSamples));
        } else {
            Vec3 halfX = stepX.multiply(0.5f);
            Vec3 halfY = stepY.multiply(0.5f);

            Color c1 = adaptiveSample(camera,              topLeft,         halfX, halfY, depth + 1, objects, lights, iorStack, rayTraceDepth);
            Color c2 = adaptiveSample(camera, topLeft.add(halfX),            halfX, halfY, depth + 1, objects, lights, iorStack, rayTraceDepth);
            Color c3 = adaptiveSample(camera, topLeft.add(halfX).add(halfY), halfX, halfY, depth + 1, objects, lights, iorStack, rayTraceDepth);
            Color c4 = adaptiveSample(camera, topLeft.add(halfY),            halfX, halfY, depth + 1, objects, lights, iorStack, rayTraceDepth);

            Vec3 avg = c1.getVector().add(c2.getVector()).add(c3.getVector()).add(c4.getVector()).divide(4f);
            return new Color(avg);
        }
    }

    /**
     * Recursively traces a ray through the scene, computing local illumination,
     * reflections, refractions, and indirect lighting via path tracing.
     *
     * @param ray the ray to trace
     * @param objects list of scene objects
     * @param lights list of lights
     * @param camera the camera viewing the scene
     * @param iorStack stack managing index of refraction states for nested transparent objects
     * @param depth recursion depth limit for ray tracing
     * @return computed color for the ray intersection
     */
    private static Color traceRay(Ray ray, List<SceneObject> objects, List<Light> lights, Camera camera, Stack<Float> iorStack, int depth) {
        if (depth <= 0) return Color.BLACK;

        Intersection nearestIntersection = ray.getNearestIntersection(objects);
        if (nearestIntersection == null) return Color.BLACK;

        SceneObject hitObject = nearestIntersection.object();
        Material material = nearestIntersection.material();
        Vec3 hitPoint = ray.p().add(ray.v().multiply(nearestIntersection.distance()));

        Vec3 normal = nearestIntersection.normal();
        Vec3 viewDir = ray.v().normalize();

        float currentIOR = iorStack.isEmpty() ? 1.0f : iorStack.peek();
        float materialRoughness = material.getRoughness();
        float materialTransmission = material.getTransmission();
        Vec3 materialAlbedo = material.getAlbedo().getVector();

        // Compute soft shadows with optimized light filtering
        List<Light> relevantLights = computeSoftShadows(nearestIntersection, lights, objects);
        LightingContext context = new LightingContext(relevantLights, hitObject, nearestIntersection, camera, Vec3.ZERO, currentIOR);
        Color localColor = cookTorranceLighting.getFinalColor(context);

        // INDIRECT LIGHTING (with Path Tracing) - diffuse reflection
        Vec3 indirect = Vec3.ZERO;
        if (DIFFUSE_REFLECTION_SAMPLES > 0) {
            // Pre-allocate variables outside loop
            Vec3 bounceRadiance = Vec3.ZERO;
            Vec3 materialContribution = Vec3.ZERO;
            Vec3 brdf = materialAlbedo.divide((float) Math.PI);

            for (int i = 0; i < DIFFUSE_REFLECTION_SAMPLES; i++) {
                Vec3 sampleDir = normal.randomHemisphereDirection();
                float pdf = pdfCosine(normal, sampleDir);
                if (pdf <= 0) continue;

                Ray bounceRay = new Ray(hitPoint.add(normal.multiply(EPSILON)), sampleDir);
                Stack<Float> bounceIorStack = new Stack<>();
                bounceIorStack.addAll(iorStack);
                Color bounceColor = traceRay(bounceRay, objects, lights, camera, bounceIorStack, depth - 1);
                bounceRadiance = bounceColor.getVector();

                Intersection bounceIntersection = bounceRay.getNearestIntersection(objects);
                float bounceRoughness = 1.0f;
                materialContribution = Vec3.ZERO;

                if (bounceIntersection != null) {
                    Material bounceMaterial = bounceIntersection.object().getMaterial();
                    bounceRoughness = bounceMaterial.getRoughness();
                    materialContribution = bounceMaterial.getAlbedo().getVector().multiply(0.1f * bounceRoughness);
                }

                bounceRadiance = bounceRadiance.add(materialContribution).multiply(bounceRoughness);
                float cosTheta = Math.max(0.0f, normal.dot(sampleDir));

                Vec3 contribution = bounceRadiance.multiply(brdf).multiply(cosTheta / pdf);
                indirect = indirect.add(contribution);
            }
            indirect = indirect.divide(DIFFUSE_REFLECTION_SAMPLES);
        }
        Color totalLocalColor = new Color(localColor.getVector().add(indirect));

        // REFLECTION (with Path Tracing) - glossy reflection
        Color reflectedColor = Color.BLACK;
        if (GLOSSY_REFLECTION_SAMPLES > 0) {
            int reflectionSamples = (materialRoughness < 0.05f) ? 1 : GLOSSY_REFLECTION_SAMPLES;
            Vec3 reflectionDir = viewDir.reflect(normal);
            Vec3 glossySum = Vec3.ZERO;

            for (int i = 0; i < reflectionSamples; i++) {
                Vec3 sampledDir = (materialRoughness < 0.05f)
                        ? reflectionDir
                        : reflectionDir.sampleGlossyDirection(normal, materialRoughness);

                Ray glossyRay = new Ray(hitPoint.add(normal.multiply(EPSILON)), sampledDir);
                Stack<Float> glossyIorStack = new Stack<>();
                glossyIorStack.addAll(iorStack);

                Intersection glossyHit = glossyRay.getNearestIntersection(objects);
                Color bounceColor;

                if (glossyHit == null && skybox != null) {
                    // skybox if reflection ray misses
                    bounceColor = skybox.sample(sampledDir);
                } else if (glossyHit != null) {
                    bounceColor = traceRay(glossyRay, objects, lights, camera, glossyIorStack, depth - 1);
                } else {
                    bounceColor = Color.BLACK;
                }

                // weight by cosine for energy conservation
                float cosTheta = Math.max(0.0f, normal.dot(sampledDir));
                glossySum = glossySum.add(bounceColor.getVector().multiply(cosTheta));
            }
            glossySum = glossySum.divide(reflectionSamples);
            reflectedColor = new Color(glossySum);
        }

        // REFRACTION - optimized IOR stack management
        boolean entering = -viewDir.dot(normal) > 0;

        float iorFrom, iorTo;
        Stack<Float> newIorStack = new Stack<>();
        newIorStack.addAll(iorStack);

        if (entering) {
            iorFrom = currentIOR;
            iorTo = material.getIor();
            newIorStack.push(material.getIor());
        } else {
            iorFrom = material.getIor();
            if (!newIorStack.isEmpty() && Math.abs(newIorStack.peek() - material.getIor()) < 1e-6f) {
                newIorStack.pop();
            }
            iorTo = newIorStack.isEmpty() ? 1.0f : newIorStack.peek();
        }

        Vec3 refractionNormal = entering ? normal : normal.multiply(-1);
        Vec3 refractionDir = viewDir.refract(refractionNormal, iorFrom, iorTo);

        Color refractedColor = Color.BLACK;

        if (refractionDir != null) {
            Vec3 offset = entering ? normal.multiply(-0.1f*EPSILON) : normal.multiply(EPSILON);
            Ray refractedRay = new Ray(nearestIntersection.point().add(offset), refractionDir);
            refractedColor = traceRay(refractedRay, objects, lights, camera, newIorStack, depth - 1);
        }

        float fresnel = cookTorranceLighting.calculateFresnel(viewDir, refractionNormal, iorFrom, iorTo);

        float reflectionWeight = fresnel;
        float transmissionWeight = (1.0f - fresnel) * materialTransmission;

        float totalWeight = reflectionWeight + transmissionWeight;
        if (totalWeight > 1.0f) {
            reflectionWeight /= totalWeight;
            transmissionWeight /= totalWeight;
        }

        float localWeight = Math.max(0.0f, 1.0f - reflectionWeight - transmissionWeight);

        Vec3 finalColor = totalLocalColor.getVector().multiply(localWeight)
                .add(reflectedColor.getVector().multiply(reflectionWeight))
                .add(refractedColor.getVector().multiply(transmissionWeight));

        return new Color(finalColor);
    }

    //PDF: Probability Density Function (how likely it is to sample a particular direction when generating random rays)
    private static float pdfCosine(Vec3 normal, Vec3 dir) {
        float cosTheta = Math.max(0.0f, normal.dot(dir.normalize()));
        return cosTheta / (float)Math.PI;
    }

    /**
     * Initializes the index of refraction stack based on the camera position,
     * checking if it starts inside any transparent objects.
     *
     * @param cameraPos camera position
     * @param objects list of scene objects
     * @return stack initialized with indices of refraction
     */
    private static Stack<Float> getInitialIorStack(Vec3 cameraPos, List<SceneObject> objects) {
        Stack<Float> iorStack = new Stack<>();

        for (SceneObject obj : objects) {
            if (obj.isInside(cameraPos)) {
                iorStack.push(obj.getMaterial().getIor());
            }
        }
        return iorStack;
    }

    private static List<Light> computeSoftShadows(Intersection hit, List<Light> lights, List<SceneObject> objects) {
        List<Light> relevantLights = new ArrayList<>();
        Vec3 point = hit.point();
        Vec3 shadowOrigin = point.add(hit.normal().multiply(EPSILON));

        for (Light light : lights) {
            float shadowCount = 0;
            boolean hasAnyLight = false;

            if (light instanceof SpotLight spot && spot.getAttenuation(point) <= 0) {
                continue;
            }

            for (int i = 0; i < SOFT_SHADOW_SAMPLES; i++) {
                Vec3 samplePos = light.jitterLightPosition();

                Vec3 toLight = samplePos.subtract(point);
                float distance = toLight.getLength();

                Ray shadowRay = new Ray(shadowOrigin, toLight);
                float transmission = 1.0f;

                for (SceneObject obj : objects) {
                    if (obj.isOccluding(shadowRay, distance)) {
                        transmission *= obj.getMaterial().getTransmission();
                        if (transmission <= 0.001f) {
                            break;
                        }
                    }
                }

                shadowCount += transmission;
                if (transmission > 0) {
                    hasAnyLight = true;
                }
            }

            if (hasAnyLight) {
                float factor = shadowCount / SOFT_SHADOW_SAMPLES;
                if (factor > 0.001f) {
                    relevantLights.add(light.copyWithIntensity(light.getIntensity() * factor));
                }
            }
        }

        return relevantLights;
    }

    /**
     * Sets up the window and GUI for displaying the image as it renders.
     */
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

    /**
     * Saves the rendered pixels to a PNG file.
     */
    private static void saveImageToFile() {
        BufferedImage image = new BufferedImage(RES_X, RES_Y, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < RES_Y; y++) {
            for (int x = 0; x < RES_X; x++) {
                image.setRGB(x, y, pixels[y * RES_X + x]);
            }
        }

        try {
            File outputfile = new File("rendered_scene.png");
            ImageIO.write(image, "png", outputfile);
            System.out.println("Image saved to " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Light> getLights() {
        return Arrays.asList(
                new Light(new Vec3(1f, 2f, 0f), 1f, 0f, Color.WHITE),
                new Light(new Vec3(0f, 0f, 0f), 1f, 0f, Color.WHITE)
        );
    }

    private static List<SceneObject> getCSG4() throws IOException {
        List<SceneObject> objects = new ArrayList<>();

        // Materials: high roughness for strong diffuse reflection
        Material redDiffuse = new Material(new Color(0.8f, 0.2f, 0.2f), 0.8f, 0.04f, 0f, 1f);
        Material floorDiffuse = new Material(new Color(1f, 1f, 1f), 0.03f, 0.02f, 0f, 1.5f);

        // Ground plane (white)
        objects.add(new Area(new Vec3(0, 1, 0), -1, floorDiffuse));

        // Red sphere above the ground
        SceneObject redSphere = new Quadric(
                new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -1f},
                redDiffuse
        ).transform(new Mat4().translate(0f, 0f, -2f));
        objects.add(redSphere);

        return objects;
    }
}
