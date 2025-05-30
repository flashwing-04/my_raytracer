package math;

public class Mat4 {
    public float[] values;

    public Mat4(float[] values) {
        if (values.length != 16) {
            throw new IllegalArgumentException("Mat4 requires exactly 16 values.");
        }
        this.values = values;
    }

    public Mat4() {
        this.values = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
    }

    public Mat4 multiply(Mat4 other) {
        float[] result = new float[16];

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += this.values[row * 4 + k] * other.values[k * 4 + col];
                }
                result[row * 4 + col] = sum;
            }
        }
        return new Mat4(result);
    }

    public Vec3 multiply(Vec3 v, float w) {
        float[] mv = this.getValues();
        float x = v.getX(), y = v.getY(), z = v.getZ();
        float nx = mv[0]*x + mv[1]*y + mv[2]*z + mv[3]*w;
        float ny = mv[4]*x + mv[5]*y + mv[6]*z + mv[7]*w;
        float nz = mv[8]*x + mv[9]*y + mv[10]*z + mv[11]*w;
        float nw = mv[12]*x + mv[13]*y + mv[14]*z + mv[15]*w;
        if (w == 1.0f && nw != 0.0f) {
            return new Vec3(nx / nw, ny / nw, nz / nw);
        } else {
            return new Vec3(nx, ny, nz);
        }
    }

    public Mat4 transpose() {
        float[] transposed = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                transposed[j * 4 + i] = values[i * 4 + j];
            }
        }
        return new Mat4(transposed);
    }

    public Mat4 translate(float x, float y, float z) {
        float[] translateMatrixValues = new float[]{
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1};

        Mat4 translateMatrix = new Mat4(translateMatrixValues);
        this.values = translateMatrix.multiply(this).values;
        return this;
    }

    public Mat4 scale(float uniformFactor) {
        float[] scaleMatrixValues = new float[]{
                uniformFactor, 0, 0, 0,
                0, uniformFactor, 0, 0,
                0, 0, uniformFactor, 0,
                0, 0, 0, 1};

        Mat4 scaleMatrix = new Mat4(scaleMatrixValues);
        this.values = scaleMatrix.multiply(this).values;
        return this;
    }

    public Mat4 scale(float sx, float sy, float sz) {
        float[] scaleMatrixValues = new float[]{
                sx, 0, 0, 0,
                0, sy, 0, 0,
                0, 0, sz, 0,
                0, 0, 0, 1};

        Mat4 scaleMatrix = new Mat4(scaleMatrixValues);
        this.values = scaleMatrix.multiply(this).values;
        return this;
    }

    public Mat4 rotateX(float angle) {
        float cos_angle = (float) Math.cos(angle);
        float sin_angle = (float) Math.sin(angle);

        float[] rotateMatrixValues = new float[]{
                1, 0, 0, 0,
                0, cos_angle, -sin_angle, 0,
                0, sin_angle, cos_angle, 0,
                0, 0, 0, 1};

        Mat4 rotateMatrix = new Mat4(rotateMatrixValues);
        this.values = rotateMatrix.multiply(this).values;
        return this;
    }

    public Mat4 rotateY(float angle) {
        float cos_angle = (float) Math.cos(angle);
        float sin_angle = (float) Math.sin(angle);

        float[] rotateMatrixValues = new float[]{
                cos_angle, 0, -sin_angle, 0,
                0, 1, 0, 0,
                sin_angle, 0, cos_angle, 0,
                0, 0, 0, 1};

        Mat4 rotateMatrix = new Mat4(rotateMatrixValues);
        this.values = rotateMatrix.multiply(this).values;
        return this;
    }

    public Mat4 rotateZ(float angle) {
        float cos_angle = (float) Math.cos(angle);
        float sin_angle = (float) Math.sin(angle);

        float[] rotateMatrixValues = new float[]{
                cos_angle, -sin_angle, 0, 0,
                sin_angle, cos_angle, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};

        Mat4 rotateMatrix = new Mat4(rotateMatrixValues);
        this.values = rotateMatrix.multiply(this).values;
        return this;
    }

    public Vec3 transform(Vec3 v) {
        float x = v.getX();
        float y = v.getY();
        float z = v.getZ();

        float[] m = this.values;

        float tx = m[0] * x + m[1] * y + m[2] * z + m[3];
        float ty = m[4] * x + m[5] * y + m[6] * z + m[7];
        float tz = m[8] * x + m[9] * y + m[10] * z + m[11];
        float tw = m[12] * x + m[13] * y + m[14] * z + m[15];

        // Handle homogeneous coordinate w
        if (tw != 0 && tw != 1) {
            tx /= tw;
            ty /= tw;
            tz /= tw;
        }

        return new Vec3(tx, ty, tz);
    }

    public Mat4 inverse() {
        float det = determinant();
        if (det == 0) {
            throw new IllegalStateException("Matrix is singular and cannot be inverted.");
        }

        //Cofactor(A[ij]) = (−1)^(i+j)*determinant_of_minor(A[ij])
        float[] cofactors = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                float cofactor = (float) Math.pow(-1, i + j) * determinantRecursive(getMinor(values, 4, i, j), 3);
                cofactors[i * 4 + j] = cofactor;
            }
        }

        Mat4 adjoint = new Mat4(cofactors).transpose();

        //Inverse = 1/det(A)*adjoint(A)
        float[] inverseValues = new float[16];
        for (int i = 0; i < 16; i++) {
            inverseValues[i] = adjoint.values[i] / det;
        }

        return new Mat4(inverseValues);
    }

    public float determinant() {
        return determinantRecursive(this.values, 4);
    }

    private float determinantRecursive(float[] matrix, int size) {
        if (size == 1) return matrix[0];
        if (size == 2) return matrix[0] * matrix[3] - matrix[1] * matrix[2];

        //det(A) = sum{i=1}^{n} (-1)^(i+j)*a[ij]*det(A[ij]) - A[ij] = minor (sub-matrix without row/col i/j)
        float det = 0;
        for (int col = 0; col < size; col++) {
            float vz = (col % 2 == 0) ? 1 : -1;
            float[] minor = getMinor(matrix, size, 0, col);
            det += vz * matrix[col] * determinantRecursive(minor, size - 1);
        }
        return det;
    }

    private float[] getMinor(float[] matrix, int size, int row, int col) {
        float[] minor = new float[(size - 1) * (size - 1)];
        int minorIndex = 0;
        for (int i = 0; i < size; i++) {
            if (i == row) continue; //Skip row that want to remove
            for (int j = 0; j < size; j++) {
                if (j == col) continue; //Skip col that want to remove
                minor[minorIndex++] = matrix[i * size + j];
            }
        }
        return minor;
    }

    public float[] getValues() {
        return values;
    }
}
