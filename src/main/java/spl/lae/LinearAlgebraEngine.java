package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.List;

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
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        List<ComputationNode> children = node.getChildren();

        switch(node.getNodeType()){
            case ADD -> {
                if (children.size() != 2)
                    throw new IllegalArgumentException("Add requires 2 operands")
                
                leftMatrix.loadRowMajor(children.getChildren(0))
                rightMatrix.loadRowMajor(children.getChildren(1))
                executor.submitAll(createAddTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            case MULTIPLY -> {
                if (children.size() != 2)
                    throw new IllegalArgumentException("Multiply requires 2 operands")
                
                leftMatrix.loadRowMajor(children.getChildren(0))
                rightMatrix.loadColumnMajor(children.getChildren(1))
                executor.submitAll(createMultiplyTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            case NEGATE -> {
                if (children.size() != 1)
                    throw new IllegalArgumentException("Negate requires 1 operand")
                
                leftMatrix.loadRowMajor(children.getChildren(0))
                executor.submitAll(createNegateTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            case TRANSPOSE -> {
                if (children.size() != 1)
                    throw new IllegalArgumentException("Transpose requires 1 operand")
                
                leftMatrix.loadRowMajor(children.getChildren(0))
                executor.submitAll(createTransposeTasks());
                node.resolve(leftMatrix.readRowMajor());
            }

            default -> throw new IllegalStateException("Unexpected node type: " + node.getNodeType());
        }
    }

    public List<Runnable> createAddTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numOfRows = leftMatrix.length();

        for(i = 0; i < numOfRows; i++){
            Runnable task = () -> leftMatrix.get(i).add(rightMatrix.get(i));
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createMultiplyTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numOfRows = leftMatrix.length();

        for(i = 0; i < numOfRows; i++){
            Runnable task = () -> leftMatrix.get(i).vecMatMul(rightMatrix);
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numOfRows = leftMatrix.length();

        for(i = 0; i < numOfRows; i++){
            Runnable task = () -> leftMatrix.get(i).negate();
            tasks.add(task);
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numOfRows = leftMatrix.length();

        for(i = 0; i < numOfRows; i++){
            Runnable task = () -> leftMatrix.get(i).transpose();
            tasks.add(task);
        }
        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
}
