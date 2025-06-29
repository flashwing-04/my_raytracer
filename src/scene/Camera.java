package scene;

import math.*;

/**
 * Represents a pinhole camera in 3D space used for ray tracing.
 *
 * <p>This camera generates rays originating from a given position and directed
 * through a rectangular image plane defined by its orientation vectors and dimensions.</p>
 *
 * <p>The camera uses a right-handed coordinate system with basis vectors:
 * <ul>
 *   <li><b>v</b>: view direction (forward)</li>
 *   <li><b>r</b>: right direction</li>
 *   <li><b>u</b>: up direction, computed as v × r</li>
 * </ul>
 * The image plane is placed at a specified distance from the camera origin along v.</p>
 */
public class Camera {

    private Vec3 position, v, r, u;
    private float heightImgPlane, widthImgPlane, distanceImgPlane;

    /**
     * Constructs a new Camera with the specified parameters.
     *
     * @param position the origin (eye point) of the camera
     * @param v the viewing (forward) direction vector
     * @param r the rightward direction vector
     * @param heightImgPlane the height of the image plane
     * @param widthImgPlane the width of the image plane
     * @param distanceImgPlane the distance from the camera position to the image plane
     */
    public Camera(Vec3 position, Vec3 v, Vec3 r, float heightImgPlane, float widthImgPlane, float distanceImgPlane) {
        this.position = position;
        this.v = v.normalize();
        this.r = r.normalize();
        this.u = v.cross(r).normalize();

        this.heightImgPlane = heightImgPlane;
        this.widthImgPlane = widthImgPlane;
        this.distanceImgPlane = distanceImgPlane;
    }

    /**
     * Computes the position of the top-left pixel (or bottom-left depending on convention)
     * on the image plane, relative to the camera's orientation.
     *
     * @return the starting point (top-left corner) of the image plane
     */
    public Vec3 getPxStart() {
        return position
                .add(v.multiply(distanceImgPlane))
                .subtract(r.multiply(widthImgPlane / 2))
                .subtract(u.multiply(heightImgPlane / 2));
    }

    /**
     * Computes the vector step in the rightward direction for one pixel in the image plane.
     *
     * @param resX the horizontal resolution (number of pixels in width)
     * @return the vector offset per pixel in the rightward direction
     */
    public Vec3 getPxRightStep(int resX) {
        return r.multiply(widthImgPlane / resX);
    }

    /**
     * Computes the vector step in the upward direction for one pixel in the image plane.
     *
     * @param resY the vertical resolution (number of pixels in height)
     * @return the vector offset per pixel in the upward direction
     */
    public Vec3 getPxUpStep(int resY) {
        return u.multiply(heightImgPlane / resY);
    }

    /** @return the position of the camera (eye point) */
    public Vec3 getPosition() {
        return position;
    }

    /** @return the viewing direction vector (v) */
    public Vec3 getV() {
        return v;
    }

    /** @return the right direction vector (r) */
    public Vec3 getR() {
        return r;
    }

    /** @return the up direction vector (u), computed as v × r */
    public Vec3 getU() {
        return u;
    }

    /** @return the height of the image plane */
    public float getHeightImgPlane() {
        return heightImgPlane;
    }

    /** @return the width of the image plane */
    public float getWidthImgPlane() {
        return widthImgPlane;
    }

    /** @return the distance from the camera to the image plane */
    public float getDistanceImgPlane() {
        return distanceImgPlane;
    }
}