package spl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import parser.ComputationNode;
import parser.ComputationNodeType;

class ComputationNodeTest {

    @Test
    void testFindResolvableSimple() {
        // Build (A + B) where A and B are matrices
        ComputationNode a = new ComputationNode(new double[][]{{1.0}});
        ComputationNode b = new ComputationNode(new double[][]{{2.0}});
        List<ComputationNode> children = List.of(a, b);
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, children);

        ComputationNode res = root.findResolvable();
        assertNotNull(res, "Resolvable node should be found");
        assertEquals(ComputationNodeType.ADD, res.getNodeType());
    }

    @Test
    void testAssociativeNesting() {
        ComputationNode a = new ComputationNode(new double[][]{{1.0}});
        ComputationNode b = new ComputationNode(new double[][]{{2.0}});
        ComputationNode c = new ComputationNode(new double[][]{{3.0}});
        List<ComputationNode> children = new ArrayList<>();
        children.add(a);
        children.add(b);
        children.add(c);

        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, children);
        root.associativeNesting();

        // After nesting, top-level should have exactly 2 children
        assertEquals(2, root.getChildren().size());
    }
}
