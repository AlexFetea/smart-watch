

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 *
 * @author mm5gg
 */
public class MyWekaUtils {

    public static Evaluation classify(String arffData) throws Exception {
        Instances instances = new Instances(new StringReader(arffData));
        instances.setClassIndex(instances.numAttributes() - 1);

        // Initialize classifier (e.g., J48 Decision Tree)
        Classifier classifier = new J48();
        classifier.buildClassifier(instances);

        // Perform 10-fold cross-validation
        Evaluation evaluation = new Evaluation(instances);
        evaluation.crossValidateModel(classifier, instances, 10, new Random(1));

        return evaluation;
    }

    public static void generateReport(Classifier classifier, Evaluation evaluation, String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Run Information
            writer.write("=== Run Information ===\n\n");
            writer.write("Scheme:       " + classifier.getClass().getName() + "\n");
            writer.write("Relation:     wada\n");
            writer.write("Instances:    " + evaluation.numInstances() + "\n");
            writer.write("Attributes:   " + evaluation.getHeader().numAttributes() + "\n");
            for (int i = 0; i < evaluation.getHeader().numAttributes(); i++) {
                writer.write("              " + evaluation.getHeader().attribute(i).name() + "\n");
            }
            writer.write("Test mode:    10-fold cross-validation\n\n");

            // Classifier Model (Decision Tree for J48)
            writer.write("=== Classifier model (full training set) ===\n\n");
            writer.write(classifier.toString() + "\n"); // Prints the model's tree structure for J48

            // Summary of Stratified Cross-Validation
            writer.write("\n=== Stratified cross-validation ===\n");
            writer.write(evaluation.toSummaryString() + "\n");

            // Detailed Accuracy by Class
            writer.write("\n=== Detailed Accuracy By Class ===\n");
            writer.write(evaluation.toClassDetailsString() + "\n");

            // Confusion Matrix
            writer.write("\n=== Confusion Matrix ===\n");
            writer.write(evaluation.toMatrixString() + "\n");

            System.out.println("Report saved to " + filePath);

        } catch (IOException e) {
            System.err.println("Error saving report to file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
        }
    }

    public static double classify(String arffData, int option) throws Exception {
		StringReader strReader = new StringReader(arffData);
		Instances instances = new Instances(strReader);
		strReader.close();
		instances.setClassIndex(instances.numAttributes() - 1);
		
		Classifier classifier;
		if(option==1)
			classifier = new J48(); // Decision Tree classifier
		else if(option==2)			
			classifier = new RandomForest();
		else if(option == 3)
			classifier = new SMO();  //This is a SVM classifier
		else 
			return -1;
		
		classifier.buildClassifier(instances); // build classifier
		
		Evaluation eval = new Evaluation(instances);
		eval.crossValidateModel(classifier, instances, 10, new Random(1), new Object[] { });
		
		return eval.pctCorrect();
	}
    
    
    public static String[][] readCSV(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        ArrayList<String> lines = new ArrayList();
        String line;

        while ((line = br.readLine()) != null) {
            lines.add(line);;
        }


        if (lines.size() == 0) {
            System.out.println("No data found");
            return null;
        }

        int lineCount = lines.size();

        String[][] csvData = new String[lineCount][];
        String[] vals;
        int i, j;
        for (i = 0; i < lineCount; i++) {            
                csvData[i] = lines.get(i).split(",");            
        }
        
        return csvData;

    }

    public static String csvToArff(String[][] csvData, int[] featureIndices) throws Exception {
        int total_rows = csvData.length;
        int total_cols = csvData[0].length;
        int fCount = featureIndices.length;
        String[] attributeList = new String[fCount + 1];
        int i, j;
        for (i = 0; i < fCount; i++) {
            attributeList[i] = csvData[0][featureIndices[i]];
        }
        attributeList[i] = csvData[0][total_cols - 1];

        String[] classList = new String[1];
        classList[0] = csvData[1][total_cols - 1];

        for (i = 1; i < total_rows; i++) {
            classList = addClass(classList, csvData[i][total_cols - 1]);
        }

        StringBuilder sb = getArffHeader(attributeList, classList);

        for (i = 1; i < total_rows; i++) {
            for (j = 0; j < fCount; j++) {
                sb.append(csvData[i][featureIndices[j]]);
                sb.append(",");
            }            
            sb.append(csvData[i][total_cols - 1]);
            sb.append("\n");
        }

        return sb.toString();
    }

    private static StringBuilder getArffHeader(String[] attributeList, String[] classList) {
        StringBuilder s = new StringBuilder();
        s.append("@RELATION wada\n\n");
    
        for (int i = 0; i < attributeList.length - 1; i++) {
            s.append("@ATTRIBUTE ");
            s.append(attributeList[i].replace(" ", "_")); // Replace spaces with underscores
            s.append(" numeric\n");
        }
    
        // Add Activity attribute as nominal
        s.append("@ATTRIBUTE ");
        s.append(attributeList[attributeList.length - 1].replace(" ", "_"));
        s.append(" {");
        s.append(String.join(",", classList)); // Use String.join for better readability
        s.append("}\n\n");
        s.append("@DATA\n");
        return s;
    }

    private static String[] addClass(String[] classList, String className) {
        int len = classList.length;
        int i;
        for (i = 0; i < len; i++) {
            if (className.equals(classList[i])) {
                return classList;
            }
        }

        String[] newList = new String[len + 1];
        for (i = 0; i < len; i++) {
            newList[i] = classList[i];
        }
        newList[i] = className;

        return newList;
    }
}
