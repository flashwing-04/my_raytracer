import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import math.*;
import math.geometry.*;
import math.geometry.objects.*;
import math.geometry.objects.csg.*;
import scene.*;
import stuff.*;
import lighting.*;
import lighting.models.*;

public class MyRaytracer {

    private static int resX = 1024;
    private static int resY = 1024;
    private static int[] pixels = new int[resX * resY];

    private static MemoryImageSource mis;

    public static void main(String[] args) {
        setUpWindow();

        Vec3 cameraPos = new Vec3(0, 0, 0);
        Vec3 cameraV = new Vec3(0, 0, -1);
        Vec3 imgPlaneR = new Vec3(1, 0, 0);
        Camera camera = new Camera(cameraPos,cameraV, imgPlaneR, 2, 2, 1);

        ArrayList<SceneObject> objects = getCSG();
        ArrayList<Light> lights = getLights();

        Vec3 pxStart = camera.getPxStart();
        Vec3 pxRightStep = camera.getPxRightStep(resX);
        Vec3 pxUpStep = camera.getPxUpStep(resY);

        for (int y = 0; y < resY; ++y) {
            for (int x = 0; x < resX; ++x) {

                Vec3 pixelPos = pxStart.add(pxRightStep.multiply(x)).add(pxUpStep.multiply(y));
                Ray ray = new Ray(camera.getPosition(),  pixelPos.subtract(camera.getPosition()));

                SceneObject nearestObject = null;
                Intersection nearestIntersection = null;
                float nearestDistance = Float.MAX_VALUE;

                for(SceneObject object : objects) {
                   List<Intersection> intersections = object.intersect(ray);

                   if(!intersections.isEmpty()) {
                       for (Intersection intersection : intersections) {
                           float s = intersection.getDistance();
                           if (s > 0 && s < nearestDistance) {
                               nearestObject = object;
                               nearestDistance = s;
                               nearestIntersection = intersection;
                           }
                       }
                   }
                }
                if(nearestObject != null) {
                    ArrayList<Light> relevantLights = new ArrayList<>();
                    for(Light light : lights) {

                        Vec3 intersectionPoint = nearestIntersection.getPoint();
                        Vec3 vectorToLight = light.getP().subtract(intersectionPoint);
                        Vec3 offset = vectorToLight.normalize().multiply(1e-3f);
                        Ray shadowRay = new Ray(intersectionPoint.add(offset), vectorToLight);

                        float distanceLight = vectorToLight.getLength();
                        boolean relevantLight = true;

                        for(SceneObject object : objects) {
                            if(object.isOccluding(shadowRay, distanceLight)) {
                                relevantLight = false;
                                break;
                            }
                        }

                        if(relevantLight) relevantLights.add(light);
                    }
                    pixels[y * resX + x] = new CookTorranceLighting(relevantLights, nearestObject, nearestIntersection, camera, new Vec3(0.0f, 0.0f, 0.0f)).getFinalColor();
                                         //new LambertLighting(lights, nearestObject, nearestIntersection).getFinalColor();
                }
            }
        }
        mis.newPixels();
    }

    public static void setUpWindow() {
        //fillBackground(pixels, resX, resY);

        mis = new MemoryImageSource(resX, resY, new DirectColorModel(24, 0xff0000, 0xff00, 0xff), pixels, 0, resX);
        mis.setAnimated(true);
        Image image = Toolkit.getDefaultToolkit().createImage(mis);

        JFrame frame = new JFrame("My Raytracer");
        frame.add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void fillBackground(int[] pixels, int resX, int resY) {
        for (int y = 0; y < resY; ++y) {
            for (int x = 0; x < resX; ++x) {
                boolean isLight = ((x / 64) + (y / 64)) % 2 == 0;
                int grey = isLight ? 0xD0D0D0 : 0xB0B0B0;
                pixels[y * resX + x] = grey;
            }
        }
    }

    public static ArrayList<Light> getLights(){
        ArrayList<lighting.Light> lights = new ArrayList<>();

        lights.add(new lighting.Light(new Vec3(1, 1, 2), new Vec3(0, 0, -1), 1f, new Color(1, 1, 1)));
        //lights.add(new lighting.Light(new Vec3(-3, 0, 0), new Vec3(0, 0, -1), 1f, new Color(1, 1, 1)));

        return lights;
    }

    public static ArrayList<SceneObject> getQuadrik() {
        ArrayList<SceneObject> objects = new ArrayList<>();
        Mat4 transform = new Mat4().translate(-3.5f, 0, -3);
        Mat4 transform2 = new Mat4().translate(0.75f, 0.25f, -1);
        Quadrik q1 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(0.0f, 0.60f, 0.30f), 0.6f, 0.001f, 0)).transform(transform);
        Quadrik q2 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -0.4f},new Material(new Color(0.0f, 0.60f, 0.30f), 0.6f, 0.001f, 0));

        //SceneObject cube = makeCube(new Material(new Color(0.0f, 0.60f, 0.30f), 0.6f, 0.001f, 0)).transform(transform2);
        //objects.add(cube);

        Material material = new Material(new Color(0.0f, 0.60f, 0.30f), 0.6f, 0.001f, 0);
        SceneObject sphere = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -0.6f},material);
        SceneObject cube = makeCube(material);
        DifferenceObject union1 = new DifferenceObject( cube, sphere, material);

        objects.add(union1);
        objects.add(q2);
        return objects;
    }

    public static ArrayList<SceneObject> getCSG() {
        ArrayList<SceneObject> objects = new ArrayList<>();

        Material material = new Material(new Color(1f, 0.2f, 0.3f), 0.1f, 0.01f, 0);
        Mat4 transform = new Mat4().rotateY(0.5f).translate(0, 0, -3);

        SceneObject sphere = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -0.6f},material);
        SceneObject cube = makeCube(material);
        SceneObject cylinder1 = new Quadrik(new float[] {1, 1, 0, 0, 0, 0, 0, 0, 0, -0.2f},material);
        SceneObject cylinder2 = new Quadrik(new float[] {0, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f},material);


        IntersectionObject intersect1 = new IntersectionObject(sphere, cube, material);
        DifferenceObject difference1 = new DifferenceObject(intersect1, cylinder1, material);
        DifferenceObject difference2 = new DifferenceObject(difference1, cylinder2, material).transform(transform);

        objects.add(difference2);

        SceneObject sphere2 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -0.2f},material);
        sphere2 = sphere2.transform(transform);

        objects.add(sphere2);

        return objects;
    }

    public static SceneObject makeCube(Material material) {
        List<SceneObject> faces = Arrays.asList(
                new Quadrik(new float[]{0,0,0,0,0,0,-1, 0, 0,-1}, material), // x ≥ -1
                new Quadrik(new float[]{0,0,0,0,0,0, 1, 0, 0,-1}, material), // x ≤ +1
                new Quadrik(new float[]{0,0,0,0,0,0, 0,-1, 0,-1}, material), // y ≥ -1
                new Quadrik(new float[]{0,0,0,0,0,0, 0, 1, 0,-1}, material), // y ≤ +1
                new Quadrik(new float[]{0,0,0,0,0,0, 0, 0,-1,-1}, material), // z ≥ -1
                new Quadrik(new float[]{0,0,0,0,0,0, 0, 0, 1,-1}, material)  // z ≤ +1
        );

        SceneObject cube = faces.getFirst();
        for (int i = 1; i < faces.size(); i++) {
            cube = new IntersectionObject(cube, faces.get(i), material);
        }

        return cube;
    }
}
