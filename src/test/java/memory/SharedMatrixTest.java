package memory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SharedMatrixTest {

    @Test
    void testConstructAndReadRowMajor() {
        double[][] mat = new double[][]{{1.0, 2.0}, {3.0, 4.0}};
        SharedMatrix m = new SharedMatrix(mat);

        assertEquals(2, m.length(), "Matrix should have two rows");
        double[][] out = m.readRowMajor();
        assertArrayEquals(mat[0], out[0], 1e-6);
        assertArrayEquals(mat[1], out[1], 1e-6);
    }

    @Test
    void testLoadColumnMajor() {
        // Create the same matrix but load it as column-major
        double[][] cols = new double[][]{{1.0, 3.0}, {2.0, 4.0}}; // columns
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(cols);

        double[][] rows = m.readRowMajor();
        assertArrayEquals(new double[]{1.0, 2.0}, rows[0], 1e-6);
        assertArrayEquals(new double[]{3.0, 4.0}, rows[1], 1e-6);
    }

    @Test
    void testGetVectorLengthAndOrientation() {
        double[][] mat = new double[][]{{5.0, 6.0}};
        SharedMatrix m = new SharedMatrix(mat);

        SharedVector v = m.get(0);
        assertEquals(2, v.length(), "Row vector should have length 2");
        // Orientation depends on implementation; at least calling getOrientation must not crash
        v.getOrientation();
    }
}
