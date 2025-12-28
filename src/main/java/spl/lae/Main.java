package spl.lae;
import java.io.IOException;

import parser.ComputationNode;
import parser.InputParser;
import parser.OutputWriter;

public class Main {
  public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java -jar lga-1.0.jar <threads> <input> <output>");
            return;
        }

        String outputPath = args[2];

        try {
            int numThreads = Integer.parseInt(args[0]);
            String inputPath = args[1];

            InputParser parser = new InputParser();
            ComputationNode root = parser.parse(inputPath);

            LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);
            ComputationNode resolvedRoot = engine.run(root);

            try {
                OutputWriter.write(resolvedRoot.getMatrix(), outputPath);
            } catch (IOException ioe) {
                System.err.println("Failed to write output: " + ioe.getMessage());
                ioe.printStackTrace(System.err);
            }

        } catch (Exception e) {
            System.err.println("Processing error: " + e.getMessage());
            try {
                OutputWriter.write(e.getMessage(), outputPath);
            } catch (IOException ioe) {
                System.err.println("Failed to write error output: " + ioe.getMessage());
                ioe.printStackTrace(System.err);
            }
        }
    }
}