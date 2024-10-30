import java.io.IOException;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import java.io.StringReader;

public class CsvToArffClassifier {

    public static void main(String[] args) {
        try {
            String csvFilePath = "feature_sets/features_part-3_window_4.csv";
            String[][] csvData = MyWekaUtils.readCSV(csvFilePath);

            if (csvData == null || csvData.length == 0) {
                System.out.println("CSV data is empty or file not found.");
                return;
            }

            int[] featureIndices = {0, 1, 2, 3, 4, 5};
            String arffData = MyWekaUtils.csvToArff(csvData, featureIndices);

            Instances instances = new Instances(new StringReader(arffData));
            instances.setClassIndex(instances.numAttributes() - 1);


            Classifier classifier = new J48();
            classifier.buildClassifier(instances);

            Evaluation evaluation = new Evaluation(instances);
            evaluation.crossValidateModel(classifier, instances, 10, new java.util.Random(1));


            String reportFilePath = "classification_reports/classification_report_features_part-3_window_4.txt";
            MyWekaUtils.generateReport(classifier, evaluation, reportFilePath);

        } catch (IOException e) {
            System.err.println("Error reading the CSV file.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred.");
            e.printStackTrace();
        }
    }
}
