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
import math.geometry.objects.csg.*;
import math.geometry.objects.sdf.*;
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

    private static final float EPSILON = 1e-4f;

    private static MemoryImageSource imageSource;

    private static CubeMap skybox = null;
    private static final CookTorranceLighting cookTorranceLighting = new CookTorranceLighting();
    private static final int PATH_TRACING_SAMPLES = 4;
    private static final int SOFT_SHADOW_SAMPLES = 1;
    private static final int MAX_SUPERSAMPLING_DEPTH = 0;
    private static final float COLOR_THRESHOLD = 0.02f;

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
        Vec3 cameraPos = new Vec3(0, 0, 0f);
        Vec3 cameraV = new Vec3(0, 0, -1);
        Vec3 imgPlaneR = new Vec3(1, 0, 0);
        Camera camera = new Camera(cameraPos, cameraV, imgPlaneR, 2, 2, 1);

        // Load scene objects and lights
        List<SceneObject> sceneObjects = getCSG2();
        List<Light> lights = getLights();

        // Load skybox textures
        BufferedImage posX = ImageIO.read(new File("src/scene/environment/posx.jpg"));
        BufferedImage negX = ImageIO.read(new File("src/scene/environment/negx.jpg"));
        BufferedImage posY = ImageIO.read(new File("src/scene/environment/posy.jpg"));
        BufferedImage negY = ImageIO.read(new File("src/scene/environment/negy.jpg"));
        BufferedImage posZ = ImageIO.read(new File("src/scene/environment/posz.jpg"));
        BufferedImage negZ = ImageIO.read(new File("src/scene/environment/negz.jpg"));
        skybox = new CubeMap(posX, negX, posY, negY, posZ, negZ);

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

        for (int y = 0; y < RES_Y; ++y) {
            final int row = y;
            executor.submit(() -> {
                for (int x = 0; x < RES_X; ++x) {
                    Vec3 pixelTopLeft = pxStart.add(stepRight.multiply(x)).add(stepUp.multiply(row));

                    // Prepare index of refraction stack for handling nested transparent objects
                    Stack<Float> initialIorStack = getInitialIorStack(camera.getPosition(), objects);

                    // Perform adaptive sampling to calculate pixel color
                    Color color = adaptiveSample(camera, pixelTopLeft, stepRight, stepUp, 0, objects, lights, initialIorStack);
                    pixels[row * RES_X + x] = color.toHex();
                }
                synchronized (imageSource) {
                    imageSource.newPixels();
                }
                long elapsedTime = System.nanoTime() - startTime;
                double seconds = elapsedTime / 1_000_000_000.0;
                System.out.printf("Rendered row %d - Time elapsed: %.2f seconds%n", row, seconds);
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
     * Performs adaptive supersampling on a pixel region by recursively subdividing
     * the pixel area until color variation is below a threshold or maximum depth reached.
     *
     * @param camera the camera
     * @param topLeft top-left position of the pixel area in image plane space
     * @param stepX horizontal pixel step vector
     * @param stepY vertical pixel step vector
     * @param depth current recursion depth
     * @param objects list of scene objects
     * @param lights list of lights
     * @param iorStack current index of refraction stack
     * @return sampled color for the pixel area
     */
    private static Color adaptiveSample(Camera camera, Vec3 topLeft, Vec3 stepX, Vec3 stepY, int depth,
                                        List<SceneObject> objects, List<Light> lights, Stack<Float> iorStack) {
        Vec3[] positions = new Vec3[4];
        positions[0] = topLeft;
        positions[1] = topLeft.add(stepX);
        positions[2] = topLeft.add(stepX).add(stepY);
        positions[3] = topLeft.add(stepY);

        Color[] colors = new Color[4];
        for (int i = 0; i < 4; i++) {
            Ray ray = new Ray(camera.getPosition(), positions[i].subtract(camera.getPosition()));
            Stack<Float> stackCopy = new Stack<>();
            stackCopy.addAll(iorStack);
            colors[i] = traceRay(ray, objects, lights, camera, stackCopy, 7);
        }

        boolean similar = true;
        for (int i = 0; i < 4 && similar; i++) {
            for (int j = i + 1; j < 4 && similar; j++) {
                if (!colors[i].similar(colors[j], COLOR_THRESHOLD)) {
                    similar = false;
                }
            }
        }

        if (similar || depth >= MAX_SUPERSAMPLING_DEPTH) {
            Vec3 finalColor = Vec3.ZERO;
            for (Color c : colors) {
                finalColor = finalColor.add(c.getVector());
            }
            return new Color(finalColor.divide(4f));
        } else {
            Vec3 halfX = stepX.multiply(0.5f);
            Vec3 halfY = stepY.multiply(0.5f);

            Color c1 = adaptiveSample(camera, topLeft, halfX, halfY, depth + 1, objects, lights, iorStack);
            Color c2 = adaptiveSample(camera, topLeft.add(halfX), halfX, halfY, depth + 1, objects, lights, iorStack);
            Color c3 = adaptiveSample(camera, topLeft.add(halfX).add(halfY), halfX, halfY, depth + 1, objects, lights, iorStack);
            Color c4 = adaptiveSample(camera, topLeft.add(halfY), halfX, halfY, depth + 1, objects, lights, iorStack);

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

        List<Light> relevantLights = computeSoftShadows(nearestIntersection, lights, objects);
        LightingContext context = new LightingContext(relevantLights, hitObject, nearestIntersection, camera, Vec3.ZERO, currentIOR);
        Color localColor = cookTorranceLighting.getFinalColor(context);

        // INDIRECT LIGHTING (with Path Tracing) - diffuse reflection
        Vec3 indirect = Vec3.ZERO;
        if (PATH_TRACING_SAMPLES > 0) {
            for (int i = 0; i < PATH_TRACING_SAMPLES; i++) {
                Vec3 sampleDir = normal.randomHemisphereDirection();
                float pdf = pdfCosine(normal, sampleDir);
                if (pdf <= 0) continue;

                Ray bounceRay = new Ray(hitPoint.add(normal.multiply(EPSILON)), sampleDir);
                Stack<Float> bounceIorStack = new Stack<>();
                bounceIorStack.addAll(iorStack);
                Color bounceColor = traceRay(bounceRay, objects, lights, camera, bounceIorStack, depth - 1);
                Vec3 bounceRadiance = bounceColor.getVector();

                Intersection bounceIntersection = bounceRay.getNearestIntersection(objects);
                float bounceRoughness = 1.0f;
                Vec3 materialContribution = Vec3.ZERO;

                if (bounceIntersection != null) {
                    Material bounceMaterial = bounceIntersection.object().getMaterial();
                    bounceRoughness = bounceMaterial.getRoughness();
                    materialContribution = bounceMaterial.getAlbedo().getVector().multiply(0.1f * bounceRoughness);
                }

                bounceRadiance = bounceRadiance.add(materialContribution).multiply(bounceRoughness);

                Vec3 brdf = material.getAlbedo().getVector().divide((float) Math.PI);
                float cosTheta = Math.max(0.0f, normal.dot(sampleDir));

                Vec3 contribution = bounceRadiance.multiply(brdf).multiply(cosTheta / pdf);
                indirect = indirect.add(contribution);
            }
            indirect = indirect.divide(PATH_TRACING_SAMPLES);
        }
        Color totalLocalColor = new Color(localColor.getVector().add(indirect));

        // REFLECTION (with Path Tracing) - glossy reflection
        Color reflectedColor = Color.BLACK;
        if (PATH_TRACING_SAMPLES > 0) {
            int reflectionSamples = (material.getRoughness() < 0.05f) ? 1 : PATH_TRACING_SAMPLES;
            Vec3 reflectionDir = viewDir.reflect(normal);
            Vec3 glossySum = Vec3.ZERO;

            for (int i = 0; i < reflectionSamples; i++) {
                Vec3 sampledDir = (material.getRoughness() < 0.05f)
                        ? reflectionDir
                        : reflectionDir.sampleGlossyDirection(normal, material.getRoughness());

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

        // REFRACTION
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
            Vec3 offset = entering ? normal.multiply(-EPSILON) : normal.multiply(EPSILON);
            Ray refractedRay = new Ray(nearestIntersection.point().add(offset), refractionDir);
            refractedColor = traceRay(refractedRay, objects, lights, camera, newIorStack, depth - 1);
        }

        float fresnel = cookTorranceLighting.calculateFresnel(viewDir, refractionNormal, iorFrom, iorTo);

        float reflectionWeight = fresnel;
        float transmissionWeight = (1.0f - fresnel) * material.getTransmission();

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
        List<SceneObject> containingObjects = new ArrayList<>();

        for (SceneObject obj : objects) {
            if (obj.isInside(cameraPos)) {
                containingObjects.add(obj);
            }
        }
        for (SceneObject obj : containingObjects) {
            iorStack.push(obj.getMaterial().getIor());
        }
        return iorStack;
    }

    private static List<Light> computeSoftShadows(Intersection hit, List<Light> lights, List<SceneObject> objects) {
        List<Light> relevantLights = new ArrayList<>();
        Vec3 point = hit.point();

        for (Light light : lights) {
            float shadowCount = 0;

            for (int i = 0; i < SOFT_SHADOW_SAMPLES; i++) {
                Vec3 samplePos = light.jitterLightPosition();
                Vec3 toLight = samplePos.subtract(point);
                float distance = toLight.getLength();

                Ray shadowRay = new Ray(point.add(toLight.normalize().multiply(EPSILON)), toLight);
                float transmission = 1.0f;

                for (SceneObject obj : objects) {
                    float t = obj.getMaterial().getTransmission();
                    if (obj.isOccluding(shadowRay, distance)) {
                        transmission *= t;
                    }
                }

                if (light instanceof SpotLight spot && spot.getAttenuation(point) <= 0) {
                    transmission = 0;
                }

                shadowCount += transmission;
            }

            float factor = (shadowCount)/ SOFT_SHADOW_SAMPLES;
            if (factor > 0) {
                relevantLights.add(light.copyWithIntensity(light.getIntensity() * factor));
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
                new SpotLight(new Vec3(0, 0, 3), 1f, 0, Color.WHITE, new Vec3(0, 0, -1), (float) Math.toRadians(15), 1f),
                new Light(new Vec3(1, 1, 2), 1f, 0,  Color.WHITE)


                //new Light(new Vec3(1.2f, -0.5f, -2.5f), 1f, Color.WHITE)
        );
    }

    private static List<Light> getLights2() {
        return Arrays.asList(
                new Light(new Vec3(1.2f, 0.2f, -3f), 1f, 0,  Color.WHITE), // main light, above and to the side
                new Light(new Vec3(0.01f, 0, 0), 1f, 0, Color.WHITE) // main light, above and to the side

                //new Light(new Vec3(0, 1, 2), 1f, Color.WHITE)
        );
    }

    private static List<SceneObject> getCSG() {
        List<SceneObject> objects = new ArrayList<>();

        Material green = new Material(new Color(0.2f, 0.5f, 0.3f), 0.5f, 0.01f, 0.0f, 1f);
        Material redish = new Material(new Color(0.5f, 0.2f, 0.3f), 0.9f, 0.01f, 0, 1f);
        Material glass = new Material(new Color(0.23f, 0.71f, 0.35f), 0.01f, 0.01f, 1f, 1.5f);
        Material air = new Material(new Color(1f, 1f, 1f), 0.01f, 0.01f, 1f, 1f);

        objects.add(new Area(new Vec3(0, 1, 0), -1f, redish));

        Mat4 transform = new Mat4().rotateY(0.5f).translate(0, 0, -3);
        SceneObject baseSphere = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.6f}, green);
        SceneObject cube = makeCube(redish);
        SceneObject cylX = new Quadric(new float[]{1, 1, 0, 0, 0, 0, 0, 0, 0, -0.2f}, green);
        SceneObject cylY = new Quadric(new float[]{0, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f}, green);
        SceneObject cylZ = new Quadric(new float[]{1, 0, 1, 0, 0, 0, 0, 0, 0, -0.2f}, green);

        SceneObject csgShape = new DifferenceObject(new DifferenceObject(new DifferenceObject(new IntersectionObject(baseSphere, cube, redish), cylX, redish), cylY, redish), cylZ, green).transform(transform);
        SceneObject greenSphere = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f}, glass).transform(transform);


        SceneObject greenSphereI = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.15f}, glass);

        SceneObject d = new DifferenceObject(greenSphere, greenSphereI, glass).transform(transform);

        SceneObject greenSphere2 = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f}, glass);
        SceneObject greenSphere2I = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.19f}, glass);

        SceneObject d2 = new DifferenceObject(greenSphere2, greenSphere2I, glass).transform(new Mat4().translate(-0.5f,-0.5f,-2));;
        objects.add(d2);

        objects.add(csgShape);
        objects.add(greenSphere);

        return objects;
    }

    private static List<SceneObject> getCSG2() {
        List<SceneObject> objects = new ArrayList<>();

        Material redish = new Material(new Color(0.5f, 0.2f, 0.3f), 0.5f, 0.01f, 0, 1f);
        Material greenish = new Material(new Color(0.23f, 0.71f, 0.35f), 0.01f, 0.9f, 0f, 1.5f);

        objects.add(new Area(new Vec3(0, 1, 0), -1.5f, redish));

        Mat4 transform = new Mat4().translate(0f, 0, -3);
        //SceneObject cube = makeCube(redish).transform(transform);

        SceneObject greenSphere = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -1f}, redish)
                .transform(transform);
        SceneObject greenSphere2 = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -1f}, redish).transform(new Mat4().translate(1f,0f,-3f));

        //SceneObject greenSphere = new Sphere(new Vec3(0,0,-3), 1f, redish);;
        Material air = new Material(new Color(1f, 1f, 1f), 0.01f, 0.01f, 1, 1f);

        //SceneObject innerSphere = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.19f}, air)
          //      .transform(new Mat4().translate(0f,0f,-3f));

        SDFObject superE = new SuperEllipsoid(1, 1, 1, 1f, 1f, greenish).transform(new Mat4().translate(0,0f,-3f));
        SDFObject superE2 = new SuperEllipsoid(1, 1, 1, 1, 1, greenish).transform(new Mat4().translate(0.4f,0f,-3f));
        float a1=1, a2=1, a3=1, e1 =1, e2 = 1;
        Mat4 translation = new Mat4().translate(0f, 0f, -3f);
        SuperEllipsoid ellipsoid = new SuperEllipsoid(a1, a2, a3, e1, e2, translation, greenish);

        Vec3 insidePoint = new Vec3(0, 0, -3);
        Vec3 outsidePoint = new Vec3(500, 500, 500);

        SceneObject torus = new Torus(2, 1, redish).transform(new Mat4().translate(0,0,-3f));
        SceneObject test = new IntersectionObject(superE2, superE, redish);
        //SceneObject specialQ = new QuarticSurface(-2, 1.5f, redish).transform(transform);
        //SceneObject d = new DifferenceObject(greenSphere, innerSphere, redish);
        //objects.add(cube);
        //objects.add(greenSphere);
        //objects.add(d);
        objects.add(test);
        //objects.add(greenSphere2);
        return objects;
    }

    private static List<SceneObject> getCSG3() {
        List<SceneObject> objects = new ArrayList<>();

        Material redish = new Material(new Color(0.5f, 0.2f, 0.3f), 0.9f, 0.01f, 0, 1f);
        Material greenish = new Material(new Color(0.23f, 0.71f, 0.35f), 0.95f, 0.01f, 0f, 1f);

        objects.add(new Area(new Vec3(0, 1, 0), -1, redish));

        Mat4 transform = new Mat4().rotateY(0.5f).translate(1f, 0, -7);
        //SceneObject cube = makeCube(redish).transform(transform);

        SceneObject greenSphere = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -1f}, greenish)
                .transform(new Mat4().translate(0f,0.15f,-3f));

        Material air = new Material(new Color(1f, 1f, 1f), 0.01f, 0.01f, 1, 1f);

        //SceneObject innerSphere = new Quadric(new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.19f}, air)
        //        .transform(new Mat4().translate(0f,0f,-3f));

        //SceneObject d = new DifferenceObject(greenSphere, innerSphere, greenish);
        //objects.add(cube);
        objects.add(greenSphere);
        //objects.add(d);

        return objects;
    }

    private static List<SceneObject> getCSG4() {
        List<SceneObject> objects = new ArrayList<>();

        // Materials: high roughness for strong diffuse reflection
        Material redDiffuse = new Material(new Color(0.8f, 0.2f, 0.2f), 0.9f, 0.01f, 0f, 1f); // diffuse red
        Material whiteDiffuse = new Material(new Color(0.2f, 0.2f, 0.2f), 0.01f, 0.01f, 0f, 1.5f); // diffuse white

        // Ground plane (white)
        objects.add(new Area(new Vec3(0, 1, 0), -1, whiteDiffuse));

        // Red sphere above the ground
        SceneObject redSphere = new Quadric(
                new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -1f}, // unit sphere
                redDiffuse
        ).transform(new Mat4().translate(0f, 0f, -3f));
        objects.add(redSphere);

        // Optionally, add a green diffuse sphere for color bleeding
        Material greenDiffuse = new Material(new Color(0.2f, 0.8f, 0.3f), 0.99f, 0.01f, 0f, 1.5f);
        SceneObject greenSphere = new Quadric(
                new float[]{1, 1, 1, 0, 0, 0, 0, 0, 0, -0.5f},
                whiteDiffuse
        ).transform(new Mat4().translate(2f, 0f, -3f));
        objects.add(greenSphere);

        return objects;
    }


    private static SceneObject makeCube(Material material) {
        List<SceneObject> faces = Arrays.asList(
                new Quadric(new float[]{0, 0, 0, 0, 0, 0, -1, 0, 0, -1}, material),
                new Quadric(new float[]{0, 0, 0, 0, 0, 0, 1, 0, 0, -1}, material),
                new Quadric(new float[]{0, 0, 0, 0, 0, 0, 0, -1, 0, -1}, material),
                new Quadric(new float[]{0, 0, 0, 0, 0, 0, 0, 1, 0, -1}, material),
                new Quadric(new float[]{0, 0, 0, 0, 0, 0, 0, 0, -1, -1}, material),
                new Quadric(new float[]{0, 0, 0, 0, 0, 0, 0, 0, 1, -1}, material)
        );

        return faces.stream().reduce((a, b) -> new IntersectionObject(a, b, material)).orElseThrow();
    }
}
