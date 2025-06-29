package scene;

import math.Vec3;
import stuff.Color;

import java.awt.image.BufferedImage;

public class CubeMap {
    private final BufferedImage[] faces; // order: +X, -X, +Y, -Y, +Z, -Z

    public CubeMap(BufferedImage posX, BufferedImage negX,
                   BufferedImage posY, BufferedImage negY,
                   BufferedImage posZ, BufferedImage negZ) {
        this.faces = new BufferedImage[]{posX, negX, posY, negY, posZ, negZ};
    }

    public Color sample(Vec3 dir) {
        dir = dir.normalize();

        float absX = Math.abs(dir.getX());
        float absY = Math.abs(dir.getY());
        float absZ = Math.abs(dir.getZ());

        int faceIndex;
        float u, v;

        if (absX >= absY && absX >= absZ) {
            // X-major
            if (dir.getX() > 0) {
                // +X
                faceIndex = 0;
                u = -dir.getZ() / absX;
                v = -dir.getY() / absX;
            } else {
                // -X
                faceIndex = 1;
                u = dir.getZ() / absX;
                v = -dir.getY() / absX;
            }
        } else if (absY >= absX && absY >= absZ) {
            // Y-major
            if (dir.getY() > 0) {
                // +Y
                faceIndex = 2;
                u = dir.getX() / absY;
                v = dir.getZ() / absY;
            } else {
                // -Y
                faceIndex = 3;
                u = dir.getX() / absY;
                v = -dir.getZ() / absY;
            }
        } else {
            // Z-major
            if (dir.getZ() > 0) {
                // +Z
                faceIndex = 4;
                u = dir.getX() / absZ;
                v = -dir.getY() / absZ;
            } else {
                // -Z
                faceIndex = 5;
                u = -dir.getX() / absZ;
                v = -dir.getY() / absZ;
            }
        }

        u = 0.5f * (u + 1.0f);
        v = 0.5f * (v + 1.0f);

        BufferedImage face = faces[faceIndex];
        int px = Math.min((int)(u * face.getWidth()), face.getWidth() - 1);
        int py = Math.min((int)(v * face.getHeight()), face.getHeight() - 1);

        int rgb = face.getRGB(px, py);

        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        return new Color(r, g, b);    }
}

