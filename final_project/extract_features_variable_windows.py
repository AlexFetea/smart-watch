import os
import pandas as pd
import csv

folder_path = "formatted_data/Accelerometer Data"
window_sizes = [1, 2, 3, 4]

for window_size in window_sizes:
    output = f'feature_sets/ternary_class_features_window_{window_size}.csv'

    with open(output, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['Mean_X', 'Std_X', 'Mean_Y', 'Std_Y', 'Mean_Z', 'Std_Z', 'Activity'])

        for filename in os.listdir(folder_path):
            file_path = os.path.join(folder_path, filename)
            if os.path.isfile(file_path):
                df = pd.read_csv(file_path)
                df.columns = ['Timestamp', 'X', 'Y', 'Z']

                df['Timestamp'] = df['Timestamp'] // 1000

                max_timestamp = df['Timestamp'].max()

                for start_time in range(int(df['Timestamp'].min()), int(max_timestamp - window_size + 1)):
                    end_time = start_time + window_size


                    window_data = df[(df['Timestamp'] >= start_time) & (df['Timestamp'] < end_time)]
                    

                    if len(window_data) > 0:
                        window_mean = window_data[['X', 'Y', 'Z']].mean()
                        window_std = window_data[['X', 'Y', 'Z']].std()
                        
                        window_mean = window_mean.fillna(0)
                        window_std = window_std.fillna(0)
                        activity = "no_fall"
                        if "major_fall" in filename:
                            activity = 'major_fall'
                        elif "minor_fall" in filename:
                            activity = "minor_fall"
                        new_row = [
                            window_mean['X'], window_std['X'],
                            window_mean['Y'], window_std['Y'],
                            window_mean['Z'], window_std['Z'],
                            activity,
                            ]
                            #'hand_wash' if "activity1" in filename else 'no_hand_wash',
                        #]
                        writer.writerow(new_row)
