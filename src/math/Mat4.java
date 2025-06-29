package math;

/**
 * A class representing a 4x4 matrix used for transformations in 3D space.
 * This matrix supports standard transformation operations such as translation,
 * scaling, rotation, inversion, multiplication, and determinant calculation.
 *
 * The internal representation is a flat array of 16 floats in row-major order:
 * [ m00, m01, m02, m03,
 *   m10, m11, m12, m13,
 *   m20, m21, m22, m23,
 *   m30, m31, m32, m33 ]
 */
public class Mat4 {

    private final float[] values;

    /**
     * Constructs a Mat4 with the given array of 16 floats.
     *
     * @param values A float array of length 16 in row-major order.
     * @throws IllegalArgumentException if the array length is not 16.
     */
    public Mat4(float[] values) {
        if (values.length != 16) {
            throw new IllegalArgumentException("Mat4 requires exactly 16 values.");
        }
        this.values = values.clone();
    }

    /**
     * Constructs an identity matrix.
     */
    public Mat4() {
        this(new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });
    }

    /**
     * Multiplies this matrix by another matrix.
     *
     * @param other The other matrix.
     * @return The result of the multiplication as a new Mat4.
     */
    public Mat4 multiply(Mat4 other) {
        float[] result = new float[16];
        float[] a = this.values;
        float[] b = other.values;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                result[row * 4 + col] =
                        a[row * 4]     * b[col] +
                                a[row * 4 + 1] * b[col + 4] +
                                a[row * 4 + 2] * b[col + 8] +
                                a[row * 4 + 3] * b[col + 12];
            }
        }

        return new Mat4(result);
    }

    /**
     * Multiplies this matrix by a vector with an optional homogeneous coordinate.
     *
     * @param v The vector to transform.
     * @param w The homogeneous component (1 for point, 0 for direction).
     * @return The transformed vector.
     */
    public Vec3 multiply(Vec3 v, float w) {
        float x = v.getX(), y = v.getY(), z = v.getZ();
        float[] m = this.values;

        float nx = m[0]*x + m[1]*y + m[2]*z + m[3]*w;
        float ny = m[4]*x + m[5]*y + m[6]*z + m[7]*w;
        float nz = m[8]*x + m[9]*y + m[10]*z + m[11]*w;
        float nw = m[12]*x + m[13]*y + m[14]*z + m[15]*w;

        return (w == 1.0f && nw != 0.0f) ? new Vec3(nx / nw, ny / nw, nz / nw) : new Vec3(nx, ny, nz);
    }

    /**
     * Returns the transpose of this matrix.
     *
     * @return A new Mat4 representing the transpose.
     */
    public Mat4 transpose() {
        float[] t = new float[16];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                t[j * 4 + i] = values[i * 4 + j];
        return new Mat4(t);
    }

    /**
     * Applies a translation transformation to this matrix.
     *
     * @param x Translation in X-axis.
     * @param y Translation in Y-axis.
     * @param z Translation in Z-axis.
     * @return A new matrix with the translation applied.
     */
    public Mat4 translate(float x, float y, float z) {
        Mat4 translation = new Mat4(new float[]{
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1
        });
        return translation.multiply(this);
    }

    /**
     * Applies a uniform scaling transformation.
     *
     * @param s Uniform scale factor.
     * @return A new matrix with the scale applied.
     */
    public Mat4 scale(float s) {
        return scale(s, s, s);
    }

    /**
     * Applies non-uniform scaling transformation.
     *
     * @param sx Scale in X-axis.
     * @param sy Scale in Y-axis.
     * @param sz Scale in Z-axis.
     * @return A new matrix with the scale applied.
     */
    public Mat4 scale(float sx, float sy, float sz) {
        Mat4 scaling = new Mat4(new float[]{
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1
        });
        return scaling.multiply(this);
    }

    /**
     * Applies rotation around the X-axis.
     *
     * @param angle Angle in radians.
     * @return A new matrix with the rotation applied.
     */
    public Mat4 rotateX(float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
        Mat4 rotX = new Mat4(new float[]{
                1, 0, 0, 0,
                0, c, -s, 0,
                0, s, c, 0,
                0, 0, 0, 1
        });
        return rotX.multiply(this);
    }

    /**
     * Applies rotation around the Y-axis.
     *
     * @param angle Angle in radians.
     * @return A new matrix with the rotation applied.
     */
    public Mat4 rotateY(float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
        Mat4 rotY = new Mat4(new float[]{
                c, 0, -s, 0,
                0, 1, 0, 0,
                s, 0, c, 0,
                0, 0, 0, 1
        });
        return rotY.multiply(this);
    }

    /**
     * Applies rotation around the Z-axis.
     *
     * @param angle Angle in radians.
     * @return A new matrix with the rotation applied.
     */
    public Mat4 rotateZ(float angle) {
        float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
        Mat4 rotZ = new Mat4(new float[]{
                c, -s, 0, 0,
                s, c, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });
        return rotZ.multiply(this);
    }

    /**
     * Transforms a vector assuming it represents a position (w=1).
     *
     * @param v The vector to transform.
     * @return The transformed vector.
     */
    public Vec3 transform(Vec3 v) {
        return multiply(v, 1.0f);
    }

    /**
     * Computes the inverse of this matrix.
     *
     * @return A new Mat4 representing the inverse.
     * @throws IllegalStateException If the matrix is not invertible (det = 0).
     */
    public Mat4 inverse() {
        float det = determinant();
        if (det == 0) {
            throw new IllegalStateException("Matrix is singular and cannot be inverted.");
        }

        float[] cofactors = new float[16];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                cofactors[i * 4 + j] = (float) Math.pow(-1, i + j) *
                        determinantRecursive(getMinor(values, 4, i, j), 3);

        Mat4 adjoint = new Mat4(cofactors).transpose();
        float[] inv = new float[16];
        for (int i = 0; i < 16; i++)
            inv[i] = adjoint.values[i] / det;

        return new Mat4(inv);
    }

    /**
     * Computes the determinant of this matrix.
     *
     * @return The determinant as a float.
     */
    public float determinant() {
        return determinantRecursive(values, 4);
    }

    /**
     * Recursively computes the determinant of a sub-matrix.
     *
     * @param m    The matrix array.
     * @param size The matrix size (e.g., 4, 3, 2).
     * @return Determinant of the matrix.
     */
    private float determinantRecursive(float[] m, int size) {
        if (size == 1) return m[0];
        if (size == 2) return m[0] * m[3] - m[1] * m[2];

        float det = 0;
        for (int col = 0; col < size; col++) {
            float sign = (col % 2 == 0) ? 1 : -1;
            float[] minor = getMinor(m, size, 0, col);
            det += sign * m[col] * determinantRecursive(minor, size - 1);
        }
        return det;
    }

    /**
     * Returns the minor matrix excluding the specified row and column.
     *
     * @param matrix The source matrix.
     * @param size   The dimension of the matrix.
     * @param row    The row to exclude.
     * @param col    The column to exclude.
     * @return A new array representing the minor.
     */
    private float[] getMinor(float[] matrix, int size, int row, int col) {
        float[] minor = new float[(size - 1) * (size - 1)];
        int index = 0;
        for (int i = 0; i < size; i++) {
            if (i == row) continue;
            for (int j = 0; j < size; j++) {
                if (j == col) continue;
                minor[index++] = matrix[i * size + j];
            }
        }
        return minor;
    }

    /**
     * Returns a copy of the internal values in row-major order.
     *
     * @return A 16-element float array.
     */
    public float[] getValues() {
        return values.clone();
    }

    /**
     * Returns a formatted string representation of the matrix.
     *
     * @return Matrix as a string in 4x4 format.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        float[] m = this.values;
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("| %8.3f %8.3f %8.3f %8.3f |\n",
                    m[i * 4], m[i * 4 + 1], m[i * 4 + 2], m[i * 4 + 3]));
        }
        return sb.toString();
    }
}