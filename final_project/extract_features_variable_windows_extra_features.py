import os
import pandas as pd
import csv

folder_path = "formatted_data/Accelerometer Data"
window_sizes = [1, 2, 3, 4]

for window_size in window_sizes:
    output = f'feature_sets_extra/ternary_class_features_window_{window_size}.csv'

    with open(output, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow([
            'Mean_X', 'Std_X', 'Range_X', 'Max_Spike_X',
            'Mean_Y', 'Std_Y', 'Range_Y', 'Max_Spike_Y',
            'Mean_Z', 'Std_Z', 'Range_Z', 'Max_Spike_Z',
            "Total_Acc",
            'Activity', 
        ])

        for filename in os.listdir(folder_path):
            file_path = os.path.join(folder_path, filename)
            if os.path.isfile(file_path):
                df = pd.read_csv(file_path)
                df.columns = ['Timestamp', 'X', 'Y', 'Z']

                # Convert timestamps to seconds
                df['Timestamp'] = df['Timestamp'] // 1000

                max_timestamp = df['Timestamp'].max()
                min_timestamp = df['Timestamp'].min()
                
                if max_timestamp - window_size < min_timestamp:
                    end = min_timestamp+1
                else:
                    end = max_timestamp - window_size + 1

                for start_time in range(int(df['Timestamp'].min()), max(int(df['Timestamp'].min())+1, int(max_timestamp - window_size + 1))):
                    end_time = start_time + window_size

                    # Extract data for the current window
                    window_data = df[(df['Timestamp'] >= start_time) & (df['Timestamp'] < end_time)]

                    if len(window_data) > 0:
                        # Calculate basic statistics
                        window_mean = window_data[['X', 'Y', 'Z']].mean()
                        window_std = window_data[['X', 'Y', 'Z']].std()

                        # Calculate range (max - min) for each axis
                        window_range = window_data[['X', 'Y', 'Z']].max() - window_data[['X', 'Y', 'Z']].min()

                        # Calculate maximum spike (max difference between consecutive values) for each axis
                        max_spike = window_data[['X', 'Y', 'Z']].diff().abs().max()

                        # Fill NaN values with 0
                        window_mean = window_mean.fillna(0)
                        window_std = window_std.fillna(0)
                        window_range = window_range.fillna(0)
                        max_spike = max_spike.fillna(0)

                        # Determine activity label
                        activity = "no_fall"
                        if "major_fall" in filename:
                            activity = 'major_fall'
                        elif "minor_fall" in filename:
                            activity = "minor_fall"
                        
                        import math
                        total_acc = math.sqrt(window_mean['X']**2 + window_mean['Y']**2 + window_mean['Z']**2)

                        # Write the features to the CSV
                        new_row = [
                            window_mean['X'], window_std['X'], window_range['X'], max_spike['X'],
                            window_mean['Y'], window_std['Y'], window_range['Y'], max_spike['Y'],
                            window_mean['Z'], window_std['Z'], window_range['Z'], max_spike['Z'],
                            total_acc, activity,
                        ]
                        writer.writerow(new_row)