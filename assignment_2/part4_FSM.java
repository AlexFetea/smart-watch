import java.io.IOException;
//import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
//import weka.classifiers.trees.J48;
import weka.core.Instances;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
public class part4_FSM extends MyWekaUtils {
	
    public static void main(String[] args) {
		
        try {
            String csvFilePath = "feature_sets/features_part-2_window_4_median_and_rms.csv";
            String[][] csvData = MyWekaUtils.readCSV(csvFilePath);

            if (csvData == null || csvData.length == 0) {
                System.out.println("CSV data is empty or file not found.");
                return;
            }
	        List <Integer> features = new ArrayList<>();
            int[] featureIndices = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	    	double global_acc = -1;
			int while_count = 0;
	    
            while(features.size() != 12 ) {
		    int local_best_idx = -1;
		    double local_best_acc = -1; 
			while_count++;
			//System.out.print("While count: " + while_count + "\n" );
		    for (int idx =0; idx <12; idx++) {
				//System.out.println(idx);
				//System.out.println(features);
			    if (! features.contains(idx)) {
					//System.out.println("inside if\n");
				    List <Integer> temp_features = new ArrayList<>(features) ;
					//int [] temp_feat_arr = features.stream().mapToInt(Integer::intValue);
				    temp_features.add(idx);
					//System.out.println(features);
					//System.out.println(temp_features);
					//int[] test_features = {0, 2, 5};
				    String arffData = MyWekaUtils.csvToArff(csvData, temp_features.stream().mapToInt(Integer::intValue).toArray());
				    double acc = MyWekaUtils.classify(arffData, 3);
					//System.out.print("for:" + idx + " accuracy: " + acc + "\n");
				    if (acc > local_best_acc) {
						//System.out.println("updating local_best_acc\n");
					    local_best_idx = idx ;
					    local_best_acc = acc;
				    }
					
			    }
				//break;
		    }
			//System.out.println("For loop done\n");
			System.out.print("While count: " + while_count + "\n" );
			System.out.println(local_best_idx);
			System.out.println(local_best_acc);
			System.out.println("\n");
			
		    if((local_best_acc - global_acc) < 1) {
				
				System.out.println("Breaking");
			    break;
		    }
		    features.add(local_best_idx);
			global_acc = local_best_acc;
			//break;
			//System.out.println(global_acc);
			System.out.println(features);
	    }
	    System.out.println(global_acc);
			System.out.println(features);

        } catch (IOException e) {
            System.err.println("Error reading the CSV file.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred.");
            e.printStackTrace();
        }
    }
}
