

import os
import pandas as pd
import csv

accel_folder_path = "formatted_data/Accelerometer Data"
gyro_folder_path = "formatted_data/Gyroscope Data"
window_sizes = [1, 2, 3, 4]

for window_size in window_sizes:
    output = f'feature_sets_gyro/ternary_class_features_window_{window_size}.csv'

    with open(output, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow([
            'Mean_X_Acc', 'Std_X_Acc', 'Range_X_Acc', 'Max_Spike_X_Acc',
            'Mean_Y_Acc', 'Std_Y_Acc', 'Range_Y_Acc', 'Max_Spike_Y_Acc',
            'Mean_Z_Acc', 'Std_Z_Acc', 'Range_Z_Acc', 'Max_Spike_Z_Acc',
            "Total_Acc",
            'Mean_X_Gyro', 'Std_X_Gyro', 'Range_X_Gyro', 'Max_Spike_X_Gyro',
            'Mean_Y_Gyro', 'Std_Y_Gyro', 'Range_Y_Gyro', 'Max_Spike_Y_Gyro',
            'Mean_Z_Gyro', 'Std_Z_Gyro', 'Range_Z_Gyro', 'Max_Spike_Z_Gyro',
            "Activity",
        ])

        for filename in os.listdir(accel_folder_path):
            accel_file_path = os.path.join(accel_folder_path, filename)
            gyro_file_path = os.path.join(gyro_folder_path, filename)
            

            if os.path.isfile(accel_file_path) and os.path.isfile(gyro_file_path):
                accel_df = pd.read_csv(accel_file_path)
                gyro_df = pd.read_csv(gyro_file_path)
                

                accel_df.columns = ['Timestamp', 'X', 'Y', 'Z']
                gyro_df.columns = ['Timestamp', 'X', 'Y', 'Z']

                # Convert timestamps to seconds
                accel_df['Timestamp'] = accel_df['Timestamp'] // 1000
                gyro_df['Timestamp'] = gyro_df['Timestamp'] // 1000

                                # Remove duplicate timestamps, keeping the last occurrence
                gyro_df = gyro_df.drop_duplicates(subset='Timestamp', keep='last')

                # Align timestamps with accelerometer data
                gyro_df = gyro_df.set_index('Timestamp').reindex(accel_df['Timestamp'].unique(), method='ffill').reset_index()
                gyro_df.columns = ['Timestamp', 'X_Gyro', 'Y_Gyro', 'Z_Gyro']


                max_timestamp = accel_df['Timestamp'].max()
                min_timestamp = accel_df['Timestamp'].min()

                for start_time in range(int(min_timestamp), max(min_timestamp+1, int(max_timestamp - window_size + 1))):
                    end_time = start_time + window_size

                    # Accelerometer window
                    accel_window = accel_df[(accel_df['Timestamp'] >= start_time) & (accel_df['Timestamp'] < end_time)]

                    # Gyroscope window
                    gyro_window = gyro_df[(gyro_df['Timestamp'] >= start_time) & (gyro_df['Timestamp'] < end_time)]

                    if len(accel_window) > 0 and len(gyro_window) > 0:
                        # Accelerometer statistics
                        accel_mean = accel_window[['X', 'Y', 'Z']].mean()
                        accel_std = accel_window[['X', 'Y', 'Z']].std()
                        accel_range = accel_window[['X', 'Y', 'Z']].max() - accel_window[['X', 'Y', 'Z']].min()
                        accel_spike = accel_window[['X', 'Y', 'Z']].diff().abs().max()

                        # Gyroscope statistics
                        gyro_mean = gyro_window[['X_Gyro', 'Y_Gyro', 'Z_Gyro']].mean()
                        gyro_std = gyro_window[['X_Gyro', 'Y_Gyro', 'Z_Gyro']].std()
                        gyro_range = gyro_window[['X_Gyro', 'Y_Gyro', 'Z_Gyro']].max() - gyro_window[['X_Gyro', 'Y_Gyro', 'Z_Gyro']].min()
                        gyro_spike = gyro_window[['X_Gyro', 'Y_Gyro', 'Z_Gyro']].diff().abs().max()

                        # Fill NaN values with 0
                        accel_mean = accel_mean.fillna(0)
                        accel_std = accel_std.fillna(0)
                        accel_range = accel_range.fillna(0)
                        accel_spike = accel_spike.fillna(0)

                        gyro_mean = gyro_mean.fillna(0)
                        gyro_std = gyro_std.fillna(0)
                        gyro_range = gyro_range.fillna(0)
                        gyro_spike = gyro_spike.fillna(0)

                        # Determine activity label
                        activity = "no_fall"
                        if "major_fall" in filename:
                            activity = 'major_fall'
                        elif "minor_fall" in filename:
                            activity = "minor_fall"

                        import math
                        total_acc = math.sqrt(accel_mean['X']**2 + accel_mean['Y']**2 + accel_mean['Z']**2)

                        # Write features to the CSV
                        writer.writerow([
                            accel_mean['X'], accel_std['X'], accel_range['X'], accel_spike['X'],
                            accel_mean['Y'], accel_std['Y'], accel_range['Y'], accel_spike['Y'],
                            accel_mean['Z'], accel_std['Z'], accel_range['Z'], accel_spike['Z'],
                            total_acc,
                            gyro_mean['X_Gyro'], gyro_std['X_Gyro'], gyro_range['X_Gyro'], gyro_spike['X_Gyro'],
                            gyro_mean['Y_Gyro'], gyro_std['Y_Gyro'], gyro_range['Y_Gyro'], gyro_spike['Y_Gyro'],
                            gyro_mean['Z_Gyro'], gyro_std['Z_Gyro'], gyro_range['Z_Gyro'], gyro_spike['Z_Gyro'],
                            activity,
                        ])

