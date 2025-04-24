package geometry.objects;

import geometry.*;
import stuff.*;

public class Quadrik extends SceneObject{

    float[] coefficients;

    public Quadrik(float[] coefficients, Material material) {
        super(material);
        if (coefficients.length!=10) {
            throw new IllegalArgumentException("Wrong number of coefficients!");
        }
        this.coefficients = coefficients;
    }

    public Quadrik(Mat4 mat, Material material) {
        super(material);
        float[] matValues = mat.getValues();
        this.coefficients = new float[]{
                matValues[0], matValues[5], matValues[10], matValues[1], matValues[2], matValues[6], matValues[3], matValues[7], matValues[11], matValues[15]
        };
    }

    public float computeIntersectionS(Ray ray) {
        Vec3 rayOrigin = ray.getP();
        Vec3 rayDirection = ray.getV();

        float vx = rayDirection.getX(); float vy = rayDirection.getY(); float vz = rayDirection.getZ();
        float px = rayOrigin.getX(); float py = rayOrigin.getY(); float pz = rayOrigin.getZ();
        float a = coefficients[0]; float b = coefficients[1]; float c = coefficients[2]; float d = coefficients[3]; float e = coefficients[4]; float f = coefficients[5]; float g = coefficients[6]; float h = coefficients[7]; float i = coefficients[8]; float j = coefficients[9];

        float A = a * vx * vx + b * vy * vy + c * vz * vz
                + 2 * (d * vx * vy + e * vx * vz + f * vy * vz);

        float B = 2 * (
                a * px * vx + b * py * vy + c * pz * vz
                + d * (px * vy + py * vx)
                + e * (px * vz + pz * vx)
                + f * (py * vz + pz * py)
                + g * vx + h * vy + i * vz
        );

        float C = a * px * px + b * py * py + c * pz * pz
                + 2 * (d * px * py + e * px * pz + f * py * pz
                + g * px + h * py + i * pz) + j;

        float discriminant = B * B - 4 * A * C;

        if (discriminant < 0) {
            return -1;
        }

        float sqrtDisc = (float) Math.sqrt(discriminant);
        float k = (-b - (Math.signum(b) * sqrtDisc)) / 2.0f;
        float s1 = C/k;
        float s2 = k/A;

        return Math.min(s1, s2);
    }

    public Vec3 getNormal(Vec3 p) {
        float x = p.getX();
        float y = p.getY();
        float z = p.getZ();

        float nx = coefficients[0]*x + coefficients[3]*y + coefficients[4]*z + coefficients[6];
        float ny = coefficients[1]*y + coefficients[3]*x + coefficients[5]*z + coefficients[7];
        float nz = coefficients[2]*z + coefficients[4]*x + coefficients[5]*y + coefficients[8];

        return new Vec3(nx, ny, nz).normalize();
    }

    public Quadrik transform(Mat4 transformMatrix) {
        return new Quadrik((transformMatrix.inverse().transpose()).multiply(this.coefficientsToMat()).multiply(transformMatrix.inverse()), this.getMaterial());
    }

    public Mat4 coefficientsToMat(){
        float[] values = {
                coefficients[0], coefficients[3], coefficients[4], coefficients[6],
                coefficients[3], coefficients[1], coefficients[5], coefficients[7],
                coefficients[4], coefficients[5], coefficients[2], coefficients[8],
                coefficients[6], coefficients[7], coefficients[8], coefficients[9]
        };
        return new Mat4(values);
    }
}
