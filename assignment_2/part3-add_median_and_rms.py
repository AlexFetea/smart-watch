import os
import pandas as pd
import csv
import numpy as np

folder_path = "formatted_data"
window_size = 4

output = 'feature_sets/features_window_4_median_and_rms.csv'

with open(output, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    writer.writerow(['Mean X', 'Std X', 'Median X', 'RMS X',
                     'Mean Y', 'Std Y', 'Median Y', 'RMS Y',
                     'Mean Z', 'Std Z', 'Median Z', 'RMS Z', 'Activity'])

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
                    window_median = window_data[['X', 'Y', 'Z']].median()
                    window_rms = np.sqrt((window_data[['X', 'Y', 'Z']] ** 2).mean())
                    
                    window_mean = window_mean.fillna(0)
                    window_std = window_std.fillna(0)
                    window_median = window_median.fillna(0)
                    window_rms = window_rms.fillna(0)

                    new_row = [
                        window_mean['X'], window_std['X'], window_median['X'], window_rms['X'],
                        window_mean['Y'], window_std['Y'], window_median['Y'], window_rms['Y'],
                        window_mean['Z'], window_std['Z'], window_median['Z'], window_rms['Z'],
                        'hand_wash' if "activity1" in filename else 'no_hand_wash',
                    ]
                    writer.writerow(new_row)
