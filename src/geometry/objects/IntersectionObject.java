package geometry.objects;

import geometry.Intersection;
import geometry.Ray;
import geometry.Vec3;
import stuff.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IntersectionObject extends SceneObject{

    private SceneObject object1;
    private SceneObject object2;

    public IntersectionObject(SceneObject object1, SceneObject object2, Material material) {
        super(material);
        this.object1 = object1;
        this.object2 = object2;
    }

    public List<Intersection> intersect(Ray ray) {
        List<Intersection> intersections = new ArrayList<Intersection>();

        List<Intersection> intersections1 = object1.intersect(ray);
        List<Intersection> intersections2 = object2.intersect(ray);

        Collections.sort(intersections1);
        Collections.sort(intersections2);

        for (int i = 0; i < intersections1.size() - 1; i += 2) {
            Intersection entry1 = intersections1.get(i);
            Intersection exit1 = intersections1.get(i + 1);

            for (int j = 0; j < intersections2.size() - 1; j += 2) {
                Intersection entry2 = intersections2.get(j);
                Intersection exit2 = intersections2.get(j + 1);

                float maxEntry = Math.max(entry1.getDistance(), entry2.getDistance());  //furthest Exit
                float minExit = Math.min(exit1.getDistance(), exit2.getDistance());     //first Entry

                if (maxEntry < minExit) {
                    Intersection newEntry = (entry1.getDistance() > entry2.getDistance()) ? entry1 : entry2;
                    Intersection newExit = (exit1.getDistance() < exit2.getDistance()) ? exit1 : exit2;

                    intersections.add(newEntry);
                    intersections.add(newExit);
                }
            }
        }
        return intersections;
    }

    public Vec3 getNormal(Vec3 p) {
        throw new UnsupportedOperationException("Use normal from Intersection instead.");
    }
}
