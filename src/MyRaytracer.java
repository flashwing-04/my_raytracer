import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import geometry.*;
import geometry.objects.*;
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

        //ArrayList<SceneObject> objects = getObjects1();
        //ArrayList<Light> lights = getLights1();

        //ArrayList<SceneObject> objects = getObjects2();
        //ArrayList<Light> lights = getLights2();

        ArrayList<SceneObject> objects = getUnion();

        ArrayList<Light> lights = new ArrayList<>();
        lights.add(new lighting.Light(new Vec3(0, 0, 3), new Vec3(0, 0, -1), 1f, new Color(1, 1, 1)));

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
                    pixels[y * resX + x] = new LambertLighting(lights, nearestObject, nearestIntersection).computeFinalColor();
                }
            }
        }
        mis.newPixels();
    }

    public static void setUpWindow() {
        fillBackground(pixels, resX, resY);

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

    public static ArrayList<SceneObject> getObjects1() {
        ArrayList<SceneObject> objects = new ArrayList<>();

        objects.add(new Sphere( new Vec3(0, 1.5f, -4), 0.5f, new Material(new Color(0.6f, 0.85f, 0.6f), 0, 0)));
        objects.add(new Sphere( new Vec3(-1, 1.2f, -4), 0.4f, new Material(new Color(0.95f, 0.9f, 0.5f), 0, 0)));
        objects.add(new Sphere( new Vec3(1, 1.2f, -4), 0.4f, new Material(new Color(0.6f, 0.8f, 1.0f), 0, 0)));
        objects.add(new Sphere( new Vec3(1.5f, 0.7f, -4), 0.2f, new Material(new Color(0.75f, 0.65f, 0.95f), 0, 0)));
        objects.add(new Sphere( new Vec3(-1.5f, 0.7f, -4), 0.2f, new Material(new Color(0.95f, 0.7f, 0.4f), 0, 0)));
        objects.add(new Sphere( new Vec3(-1.9f, 0f, -4), 0.5f, new Material(new Color(0.9f, 0.6f, 0.6f), 0, 0)));
        objects.add(new Sphere( new Vec3(1.9f, 0f, -4), 0.5f, new Material(new Color(0.7f, 0.5f, 0.9f), 0, 0)));

        objects.add(new Area( new Vec3(0, 1, 0), -10,new Material(new Color(0.9f, 0.9f, 0.9f), 0, 0)));

        return objects;
    }

    public static ArrayList<Light> getLights1(){
        ArrayList<lighting.Light> lights = new ArrayList<>();

        lights.add(new lighting.Light(new Vec3(-3, 3, 3), new Vec3(0, 0, -1), 1f, new Color(1, 1, 1)));
        lights.add(new lighting.Light(new Vec3(0, 2, -10), new Vec3(0, 0, 1), 1f, new Color(1, 1, 1)));

        return lights;
    }

    public static ArrayList<SceneObject> getObjects2() {
        ArrayList<SceneObject> objects = new ArrayList<>();

        objects.add(new Sphere(new Vec3(-1.5f, 1.0f, -2), 0.5f, new Material(new Color(0.2f, 0.4f, 0.2f), 0, 0))); // Dunkelgrün
        objects.add(new Sphere(new Vec3(-0.2f, 1.2f, -3f), 0.5f, new Material(new Color(0.1f, 0.5f, 0.3f), 0, 0))); // Tannengrün
        objects.add(new Sphere(new Vec3(-0.3f, -2f, -4.3f), 1f, new Material(new Color(0.0f, 0.4f, 0.4f), 0, 0))); // Dunkeltürkis
        objects.add(new Sphere(new Vec3(0.3f, 1.1f, -4.5f), 0.5f, new Material(new Color(0.1f, 0.3f, 0.6f), 0, 0))); // Tiefblau
        objects.add(new Sphere(new Vec3(1.5f, 0.3f, -4f), 0.5f, new Material(new Color(0.2f, 0.4f, 0.7f), 0, 0))); // Saphirblau
        objects.add(new Sphere(new Vec3(1.4f, 1.8f, -2.4f), 0.5f, new Material(new Color(0.1f, 0.3f, 0.5f), 0, 0))); // Mitternachtsblau
        objects.add(new Sphere(new Vec3(0.0f, 0f, -3.8f), 0.4f, new Material(new Color(0.0f, 0.3f, 0.2f), 0, 0))); // Dunkles Seegrün

        objects.add(new Area( new Vec3(0, 1, 0), -10,new Material(new Color(0.9f, 0.9f, 0.9f), 0, 0)));

        return objects;
    }

    public static ArrayList<Light> getLights2(){
        ArrayList<lighting.Light> lights = new ArrayList<>();

        lights.add(new lighting.Light(new Vec3(-3, 3, 3), new Vec3(0, 0, -1), 1.0f, new Color(1.0f, 1.0f, 1.0f)));
        lights.add(new lighting.Light(new Vec3(-2, -1, 2), new Vec3(0.2f, 0.2f, -1.0f), 0.3f, new Color(0.6f, 0.9f, 0.6f)));
        lights.add(new lighting.Light(new Vec3(3, 2, -10), new Vec3(-0.2f, -0.1f, 1.0f), 0.5f, new Color(0.5f, 0.7f, 1.0f)));
        lights.add(new lighting.Light(new Vec3(0, -2, 2), new Vec3(0, 1, -0.5f), 0.8f, new Color(0.3f, 0.8f, 0.7f)));
        lights.add(new lighting.Light(new Vec3(4, 1, 0), new Vec3(-1, 0, 0), 0.1f, new Color(0.2f, 0.4f, 0.8f)));

        return lights;
    }

    public static ArrayList<SceneObject> getQuadrik() {
        ArrayList<SceneObject> objects = new ArrayList<>();
        Mat4 transform = new Mat4().translate(0, 0, -3);
        Quadrik q = new Quadrik(new float[] {1, 1, 1, 0, 1, 0, 1, 1, 0, -1},new Material(new Color(1, 0, 0), 0, 0)).transform(transform);
        objects.add(q);
        return objects;
    }

    public static ArrayList<SceneObject> getUnion() {
        ArrayList<SceneObject> objects = new ArrayList<>();
        Mat4 transform1 = new Mat4().translate(0.5f, 0, -3);
        Quadrik q1 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(1, 0, 0), 0, 0)).transform(transform1);
        Mat4 transform2 = new Mat4().translate(-0.5f, 0, -3);
        Quadrik q2 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(1, 0, 0), 0, 0)).transform(transform2);
        Mat4 transform3 = new Mat4().translate(0, 0, -3.5f);
        Quadrik q3 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(1, 0, 0), 0, 0)).transform(transform3);

        IntersectionObject i = new IntersectionObject(q1, q2, q1.getMaterial());
        DifferenceObject i2 = new DifferenceObject(q3, i, q1.getMaterial());

        objects.add(i2);
        return objects;
    }
}
