package spl.lae;

import java.util.List;

import memory.SharedMatrix;
import parser.ComputationNode;
import scheduling.TiredExecutor;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        this.executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        computationRoot.associativeNesting();
        ComputationNode curr = computationRoot.findResolvable();
        while (curr != null){
            loadAndCompute(curr);
            curr = computationRoot.findResolvable();
        }
        try {
            executor.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        List<ComputationNode> children = node.getChildren();

        switch (node.getNodeType()) {
            case ADD -> {
                if (children.size() != 2) {
                    throw new IllegalArgumentException("Add requires 2 operands");
                }

                double[][] left = children.get(0).getMatrix();
                double[][] right = children.get(1).getMatrix();

                leftMatrix.loadRowMajor(left);
                rightMatrix.loadRowMajor(right);
                executor.submitAll(createAddTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            case MULTIPLY -> {
                if (children.size() != 2) {
                    throw new IllegalArgumentException("Multiply requires 2 operands");
                }

                double[][] left = children.get(0).getMatrix();
                double[][] right = children.get(1).getMatrix();

                leftMatrix.loadRowMajor(left);
                rightMatrix.loadColumnMajor(right);
                executor.submitAll(createMultiplyTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            case NEGATE -> {
                if (children.size() != 1) {
                    throw new IllegalArgumentException("Negate requires 1 operand");
                }

                double[][] left = children.get(0).getMatrix();

                leftMatrix.loadRowMajor(left);
                executor.submitAll(createNegateTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            case TRANSPOSE -> {
                if (children.size() != 1) {
                    throw new IllegalArgumentException("Transpose requires 1 operand");
                }

                double[][] left = children.get(0).getMatrix();

                leftMatrix.loadRowMajor(left);
                executor.submitAll(createTransposeTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            default -> throw new IllegalStateException("Unexpected node type: " + node.getNodeType());
        }
    }

    public List<Runnable> createAddTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numOfRows = leftMatrix.length();

        for (int i = 0; i < numOfRows; i++) {
            final int row = i;
            Runnable task = () -> leftMatrix.get(row).add(rightMatrix.get(row));
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numOfRows = leftMatrix.length();

        for (int i = 0; i < numOfRows; i++) {
            final int row = i;
            Runnable task = () -> leftMatrix.get(row).vecMatMul(rightMatrix);
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numOfRows = leftMatrix.length();

        for (int i = 0; i < numOfRows; i++) {
            final int row = i;
            Runnable task = () -> leftMatrix.get(row).negate();
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> tasks = new java.util.ArrayList<>();
        int numOfRows = leftMatrix.length();

        for (int i = 0; i < numOfRows; i++) {
            final int row = i;
            Runnable task = () -> leftMatrix.get(row).transpose();
            tasks.add(task);
        }
        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}
