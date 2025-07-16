package scene;

import lighting.Light;
import lighting.SpotLight;
import math.Mat4;
import math.Vec3;
import math.geometry.objects.Area;
import math.geometry.objects.MeshObject;
import math.geometry.objects.Quadric;
import math.geometry.objects.SceneObject;
import math.geometry.objects.csg.DifferenceObject;
import math.geometry.objects.csg.IntersectionObject;
import math.geometry.objects.sdf.*;
import stuff.Color;
import stuff.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SceneBuilder {

    public static CubeMap getSkybox() throws IOException {
        BufferedImage posX = ImageIO.read(new File("src/scene/environment/posx.jpg"));
        BufferedImage negX = ImageIO.read(new File("src/scene/environment/negx.jpg"));
        BufferedImage posY = ImageIO.read(new File("src/scene/environment/posy.jpg"));
        BufferedImage negY = ImageIO.read(new File("src/scene/environment/negy.jpg"));
        BufferedImage posZ = ImageIO.read(new File("src/scene/environment/posz.jpg"));
        BufferedImage negZ = ImageIO.read(new File("src/scene/environment/negz.jpg"));
        return new CubeMap(posX, negX, posY, negY, posZ, negZ);
    }

    public static ArrayList<SceneObject> getObjects() throws IOException {
        ArrayList<SceneObject> sceneObjects = new ArrayList<>();

        Material yellowRubber = new Material(new Color(1.0f, 0.85f, 0.1f), 0.3f, 0.01f, 0.001f, 1.45f);
        Material bubble = new Material(new Color(1.0f, 1.0f, 1.0f), 0.04f, 0.01f, 0.99f, 1.33f);
        Material water = new Material(new Color(0.0f, 0.3f, 0.5f), 0.04f, 0.01f, 0.7f, 1.33f);

        SDFObject ripple0 = new Torus(0.1f, 0.05f, new Mat4().translate(-2f, 0.03f, -2f), water);
        SDFObject ripple1 = new Torus(0.3f, 0.1f, new Mat4().translate(-2f, 0f, -2f), water);
        SDFObject ripple2 = new Torus(0.4f, 0.15f, new Mat4().translate(-2f, -0.02f, -2f), water);
        SDFObject ripple3 = new Torus(0.8f, 0.15f, new Mat4().translate(-2f, -0.05f, -2f), water);
        SDFObject ripple4 = new Torus(2f, 0.8f, new Mat4().translate(-2f, -0.5f, -2f), water);
        SDFObject ripple5 = new Torus(4f, 1.3f, new Mat4().translate(-2f, -1f, -2f), water);
        SDFObject ripple6 = new Torus(6f, 1.5f, new Mat4().translate(-2f, -1f, -2f), water);
        SDFObject ripple7 = new Torus(8f, 1.5f, new Mat4().translate(-2f, -1f, -2f), water);
        SceneObject waterSurface = new SmoothUnionObject(ripple7, new SmoothUnionObject(ripple6, new SmoothUnionObject(ripple5, new SmoothUnionObject(ripple4, new SmoothUnionObject(ripple3, (new SmoothUnionObject(ripple2, new SmoothUnionObject(ripple0, ripple1, water, 0.3f), water, 0.3f)), water, 0.3f), water, 0.9f), water, 0.9f), water, 0.9f), water, 0.9f);
        SceneObject waterBottom = new Area(new Vec3(0f, 1f, 0f), -0.3f, new Material(new Color(0.0f, 0.3f, 0.5f), 0.2f, 0.01f, 0f, 1.33f));
        sceneObjects.add(waterSurface);
        sceneObjects.add(waterBottom);

        //File f = new File("src/scene/environment/duck.obj");
        //SceneObject duck = new MeshObject(f, yellowRubber).transform(new Mat4().rotateX(-0.1f).rotateY(0.3f).translate(-2f, 0.07f, -2f));
        //sceneObjects.add(duck);

        //SceneObject bubble1out = new SuperEllipsoid(0.4f, 0.4f, 0.4f, 1, 1, bubble).transform(new Mat4().translate(-1f, 1f, -2f));
        //SceneObject bubble1in = new SuperEllipsoid(0.38f, 0.38f, 0.38f, 1, 1, bubble).transform(new Mat4().translate(-1f, 1f, -2f));
        //SceneObject bubble1 = new DifferenceObject(bubble1out, bubble1in, bubble);
//
        //SceneObject bubble2out = new SuperEllipsoid(0.8f, 0.8f, 0.8f, 1, 1, bubble).transform(new Mat4().translate(-4f, 1.2f, -0.8f));
        //SceneObject bubble2in = new SuperEllipsoid(0.79f, 0.79f, 0.79f, 1, 1, bubble).transform(new Mat4().translate(-4f, 1.2f, -0.8f));
        //SceneObject bubble2 = new DifferenceObject(bubble2out, bubble2in, bubble);
//
        //SceneObject bubble3out = new SuperEllipsoid(0.5f, 0.5f, 0.5f, 1, 1, bubble).transform(new Mat4().translate(-0.2f, 0.3f, 0.1f));
        //SceneObject bubble3in = new SuperEllipsoid(0.485f, 0.485f, 0.485f, 1, 1, bubble).transform(new Mat4().translate(-0.2f, 0.3f, 0.1f));
        //SceneObject bubble3 = new DifferenceObject(bubble3out, bubble3in, bubble);
//
        //SceneObject bubble4out = new SuperEllipsoid(0.2f, 0.2f, 0.2f, 1, 1, bubble).transform(new Mat4().translate(-0.2f, 0.6f, -1.5f));
        //SceneObject bubble4in = new SuperEllipsoid(0.19f, 0.19f, 0.19f, 1, 1, bubble).transform(new Mat4().translate(-0.2f, 0.6f, -1.5f));
        //SceneObject bubble4 = new DifferenceObject(bubble4out, bubble4in, bubble);
//
        //sceneObjects.add(bubble1);
        //sceneObjects.add(bubble2);
        //sceneObjects.add(bubble3);
        //sceneObjects.add(bubble4);

        return sceneObjects;
    }

    public static ArrayList<Light> getLights() {
        ArrayList<Light> lights = new ArrayList<>();

        SpotLight l1 = new SpotLight(new Vec3(0, 3, 0), 0.5f, 0.0f, Color.WHITE, new Vec3(-2, -4, -2), (float) Math.toRadians(35), 1f);
        SpotLight l2 = new SpotLight(new Vec3(-5, 3, 0), 0.3f, 0.0f, Color.WHITE, new Vec3(3, -4, -2), (float) Math.toRadians(35), 1f);
        SpotLight l3 = new SpotLight(new Vec3(-1, 7, -6), 0.2f, 0.0f, Color.WHITE, new Vec3(1, -5, 4), (float) Math.toRadians(20), 1f);

        lights.add(l1);
        lights.add(l2);
        lights.add(l3);

        return lights;
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