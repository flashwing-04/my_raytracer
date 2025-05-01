import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

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

        ArrayList<SceneObject> objects = getQuadrik();
        ArrayList<Light> lights = getLights1();

        //ArrayList<Light> lights = new ArrayList<>();
        //lights.add(new lighting.Light(new Vec3(3, 2, 3), new Vec3(0, 0, -1), 1f, new Color(1, 1, 1)));

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
                    pixels[y * resX + x] = new CookTorranceLighting(lights, nearestObject, nearestIntersection, camera, new Vec3(0.0f, 0.0f, 0.0f)).getFinalColor();
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

    public static ArrayList<Light> getLights1(){
        ArrayList<lighting.Light> lights = new ArrayList<>();

        lights.add(new lighting.Light(new Vec3(-4, 4, 3), new Vec3(0, 0, -1), 2f, new Color(1, 1, 1)));
        lights.add(new lighting.Light(new Vec3(0, 2, -10), new Vec3(0, 0, 1), 1f, new Color(1, 1, 1)));

        return lights;
    }

    public static ArrayList<SceneObject> getQuadrik() {
        ArrayList<SceneObject> objects = new ArrayList<>();
        Mat4 transform = new Mat4().translate(0, 0, -3);
        Quadrik q = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(0.75f, 0.60f, 0.30f), 0.07f, 0.001f, 0)).transform(transform);
        objects.add(q);
        return objects;
    }

    public static ArrayList<SceneObject> getUnion() {
        ArrayList<SceneObject> objects = new ArrayList<>();
        Mat4 transform1 = new Mat4().translate(0.5f, 0, -3);
        Quadrik q1 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(1, 0, 0), 0.2f, 0.95f, 0)).transform(transform1);
        Mat4 transform2 = new Mat4().translate(-0.5f, 0, -3);
        Quadrik q2 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(1, 0, 0), 0.2f, 0.95f, 0)).transform(transform2);
        Mat4 transform3 = new Mat4().translate(0, 0, -3.5f);
        Quadrik q3 = new Quadrik(new float[] {1, 1, 1, 0, 0, 0, 0, 0, 0, -1},new Material(new Color(0.55f, 0.47f, 0.14f), 0.001f, 0.001f, 0)).transform(transform3);

        IntersectionObject i = new IntersectionObject(q1, q2, q3.getMaterial());
        DifferenceObject i2 = new DifferenceObject(q3, i, q3.getMaterial());

        objects.add(i2);
        return objects;
    }
}
