package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {};

    public SharedMatrix() {
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadRowMajor(double[][] matrix) {
        vectors = new SharedVector[matrix.length];
        
        for (int i = 0; i < matrix.length; i++) {
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
    }

    public void loadColumnMajor(double[][] matrix) {
        vectors = new SharedVector[matrix[0].length];
        
        for (int i = 0; i < matrix[0].length; i++) {
            double[] col = new double[matrix.length];
            
            for (int j = 0; j < matrix.length; j++) {
                col[j] = matrix[j][i];
            }
            
            vectors[i] = new SharedVector(col, VectorOrientation.COLUMN_MAJOR);
        }
    }

    public double[][] readRowMajor() {
        double[][] result = new double[vectors.length][];

        for (int i = 0; i < vectors.length; i++) {
            result[i] = new double[vectors[i].length()];
            for (int j = 0; j < vectors[i].length(); j++) {
                result[i][j] = vectors[i].get(j);
            }
        }
        return result;
    }

    public SharedVector get(int index) {
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if (vectors.length == 0) {
            return VectorOrientation.ROW_MAJOR;
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vec : vecs) {
            vec.writeUnlock();
        }
    }
}
