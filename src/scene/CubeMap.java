package scene;

import math.Vec3;
import stuff.Color;

import java.awt.image.BufferedImage;

/**
 * Represents a cubemap texture consisting of six faces.
 * Each face is a {@link BufferedImage} representing one side of the cube.
 * The faces are stored in the order: +X, -X, +Y, -Y, +Z, -Z.
 *
 * This class provides functionality to sample a color from the cubemap
 * given a 3D direction vector.
 */
public class CubeMap {

    private final BufferedImage[] faces;

    /**
     * Constructs a CubeMap from six BufferedImages representing the faces.
     *
     * @param posX Image for the positive X face
     * @param negX Image for the negative X face
     * @param posY Image for the positive Y face
     * @param negY Image for the negative Y face
     * @param posZ Image for the positive Z face
     * @param negZ Image for the negative Z face
     */
    public CubeMap(BufferedImage posX, BufferedImage negX,
                   BufferedImage posY, BufferedImage negY,
                   BufferedImage posZ, BufferedImage negZ) {
        this.faces = new BufferedImage[]{posX, negX, posY, negY, posZ, negZ};
    }

    /**
     * Samples the cubemap with a given 3D direction vector.
     * The direction vector is first normalized.
     * The face to sample is chosen based on the major component of the vector.
     * Then, the 2D coordinates (u, v) within that face are computed.
     * The color at that pixel is retrieved and converted to a normalized {@link Color}.
     *
     * @param dir The direction vector to sample (does not have to be normalized)
     * @return The color sampled from the cubemap in the given direction
     */
    public Color sample(Vec3 dir) {
        dir = dir.normalize();

        float absX = Math.abs(dir.getX());
        float absY = Math.abs(dir.getY());
        float absZ = Math.abs(dir.getZ());

        int faceIndex;  // Index of the CubeMap face to sample
        float u, v;     // 2D coordinates on the selected face

        // Determine which face is the major axis (the largest absolute component)
        if (absX >= absY && absX >= absZ) {
            // X-major axis
            if (dir.getX() > 0) {
                // Positive X face (+X)
                faceIndex = 0;
                u = -dir.getZ() / absX;
                v = -dir.getY() / absX;
            } else {
                // Negative X face (-X)
                faceIndex = 1;
                u = dir.getZ() / absX;
                v = -dir.getY() / absX;
            }
        } else if (absY >= absX && absY >= absZ) {
            // Y-major axis
            if (dir.getY() > 0) {
                // Positive Y face (+Y)
                faceIndex = 2;
                u = dir.getX() / absY;
                v = dir.getZ() / absY;
            } else {
                // Negative Y face (-Y)
                faceIndex = 3;
                u = dir.getX() / absY;
                v = -dir.getZ() / absY;
            }
        } else {
            // Z-major axis
            if (dir.getZ() > 0) {
                // Positive Z face (+Z)
                faceIndex = 4;
                u = dir.getX() / absZ;
                v = -dir.getY() / absZ;
            } else {
                // Negative Z face (-Z)
                faceIndex = 5;
                u = -dir.getX() / absZ;
                v = -dir.getY() / absZ;
            }
        }

        // Convert range from [-1, 1] to [0, 1] for texture coordinate mapping
        u = 0.5f * (u + 1.0f);
        v = 0.5f * (v + 1.0f);

        BufferedImage face = faces[faceIndex];

        int px = Math.min((int)(u * face.getWidth()), face.getWidth() - 1);
        int py = Math.min((int)(v * face.getHeight()), face.getHeight() - 1);

        int rgb = face.getRGB(px, py);

        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        return new Color(r, g, b);
    }
}