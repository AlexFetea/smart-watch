import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Assignment2Part1 {

    public static void main(String[] args) {
        try {
            // Step 1: Read CSV data
            String csvFilePath = "path/to/features.csv"; // Replace with your CSV file path
            String[][] csvData = MyWekaUtils.readCSV(csvFilePath);

            if (csvData == null) {
                System.out.println("CSV data is empty or file not found.");
                return;
            }

            // Step 2: Convert CSV to ARFF
            int[] featureIndices = {0, 1, 2, 3, 4, 5}; // Assuming six features for Part 1
            String arffData = MyWekaUtils.csvToArff(csvData, featureIndices);

            // Step 3: Classify with Decision Tree (option 1 in MyWekaUtils)
            double accuracy = MyWekaUtils.classify(arffData, 1); // 1 = Decision Tree (J48)

            // Step 4: Print the accuracy
            System.out.println("Classification Accuracy with Decision Tree: " + accuracy + "%");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
