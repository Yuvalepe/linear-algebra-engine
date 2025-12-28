package memory;

import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        readLock();
        try {
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        return this.vector.length;
    }

    public VectorOrientation getOrientation() {
        return this.orientation;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try{
            if (this.orientation == VectorOrientation.ROW_MAJOR){
                this.orientation = VectorOrientation.COLUMN_MAJOR;
            }
            else if (this.orientation == VectorOrientation.COLUMN_MAJOR) {
                this.orientation = VectorOrientation.ROW_MAJOR;
            }
        } finally {
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        if (this.orientation != other.orientation) {
            throw new IllegalArgumentException("Cannot add vectors with different orientations");
        }
        if (this.length() != other.length()) {
            throw new IndexOutOfBoundsException("Vectors must have the same length");
        }

        writeLock();
        other.readLock();
        try {
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] += other.get(i);
            }
        } finally {
            writeUnlock();
            other.readUnlock();
        }
    }

    public void negate() {
        writeLock();
        try {
            for (int i = 0; i < this.length(); i++) {
                this.vector[i] = -this.vector[i];
            }
        } finally {
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        if (other == null) {
        throw new IllegalArgumentException("Other vector cannot be null");
        }
        if (this.length() != other.length()) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        if (this.orientation == other.orientation) {
            throw new IllegalArgumentException("Dot product requires one row vector and one column vector");
        }

        readLock();
        other.readLock();
        try {
            double sum = 0.0;
            for (int i = 0; i < this.length(); i++) {
                sum += this.vector[i] * other.get(i);
            }
            return sum;
        } finally {
            other.readUnlock();
            readUnlock();
        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (this.orientation != VectorOrientation.ROW_MAJOR) {
            throw new IllegalArgumentException("Vector must be a row vector for vecMatMul");
        }

        VectorOrientation matOrien = matrix.getOrientation();
        int matRows = (matOrien == VectorOrientation.ROW_MAJOR) ? matrix.length() : matrix.get(0).length();
        int matCols = (matOrien == VectorOrientation.ROW_MAJOR) ? matrix.get(0).length() : matrix.length();
        if (this.length() != matRows){
            throw new IllegalArgumentException("Vector length must match number of matrix rows");
        }

        double[] res = new double[matCols];
        writeLock();
        try {
            if (matOrien == VectorOrientation.ROW_MAJOR) {
                for (int j = 0; j < matCols; j++) {
                    double sum = 0.0;
                    for (int i = 0; i < matRows; i++) {
                        sum += this.vector[i] * matrix.get(i).get(j);
                    }
                    res[j] = sum;
                }
            } else {
                for (int j = 0; j < matCols; j++) {
                    double sum = 0.0;
                    SharedVector col = matrix.get(j);
                    for (int i = 0; i < matRows; i++) {
                        sum += this.vector[i] * col.get(i);
                    }
                    res[j] = sum;
                }
            }
            this.vector = res;
        } finally {
            writeUnlock();
        }
    }
}
