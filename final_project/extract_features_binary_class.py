import os
import pandas as pd
import csv

folder_path = "formatted_data/Accelerometer Data"
output = 'two_class_features.csv'

with open(output, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['Mean_X', 'Std_X', 'Mean_Y', 'Std_Y', 'Mean_Z', 'Std_Z', 'Activity'])

    # Iterate over files in the folder
    for filename in os.listdir(folder_path):
        file_path = os.path.join(folder_path, filename)
        if os.path.isfile(file_path):
            df = pd.read_csv(file_path)

            df.columns = ['Timestamp', 'X', 'Y', 'Z']

            df['Timestamp'] = df['Timestamp'] // 1000

            # Group by each second based on Timestamp
            grouped = df.groupby('Timestamp')

            # Iterate over each group
            for timestamp, group in grouped:
                # Skip the last group if it is incomplete
                if timestamp == df['Timestamp'].max() and len(group) < 1000:
                    continue

                if len(group) > 0:
                    window_mean = group[['X', 'Y', 'Z']].mean()
                    window_std = group[['X', 'Y', 'Z']].std()
                    
                    window_mean = window_mean.fillna(0)
                    window_std = window_std.fillna(0)
                    activity = "no_fall"
                    if "major_fall" in filename:
                        activity = 'fall'
                    elif "minor_fall" in filename:
                        activity = "fall"
                    new_row = [
                        window_mean['X'], window_std['X'],
                        window_mean['Y'], window_std['Y'],
                        window_mean['Z'], window_std['Z'],
                        activity
                        ]
                        #'major_fall' if "major_fall" in filename elif 'minor_fall' if "minor_fall" in filename else 'no_fall'
                    #]
                    
                    writer.writerow(new_row)
