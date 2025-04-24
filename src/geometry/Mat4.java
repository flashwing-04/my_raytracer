package geometry;

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
