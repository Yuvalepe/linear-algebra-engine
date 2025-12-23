package memory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class SharedVectorTest {

    @Test
    void testSum_Success() {
        double[] d1 = {1.0, 2.0, 3.0};
        double[] d2 = {4.0, 5.0, 6.0};
        SharedVector v1 = new SharedVector(d1, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(d2, VectorOrientation.ROW_MAJOR);

        v1.add(v2);

        assertArrayEquals(new double[]{5.0, 7.0, 9.0}, extractData(v1), 0.0001, "Vector addition failed");
    }

    @Test
    void testSum_DimensionMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1.0, 2.0}, VectorOrientation.ROW_MAJOR);

        // Depending on your implementation, this should throw an exception or handle gracefully
        assertThrows(IndexOutOfBoundsException.class, () -> v1.add(v2), "Should throw exception for mismatched sizes");
    }

    @Test
    void testNegate() {
        double[] data = {10.5, -20.0, 0.0};
        SharedVector v = new SharedVector(data, VectorOrientation.COLUMN_MAJOR);
        
        v.negate();
        
        assertArrayEquals(new double[]{-10.5, 20.0, -0.0}, extractData(v), 0.0001);
    }
    
    @Test
    void testOrientationPreservation() {
        SharedVector v = new SharedVector(new double[]{1.0}, VectorOrientation.COLUMN_MAJOR);
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());
    }

    // Helper to avoid calling .get(i) repeatedly in assertions
    private double[] extractData(SharedVector v) {
        double[] res = new double[v.length()];
        for(int i=0; i<v.length(); i++) res[i] = v.get(i);
        return res;
    }
}