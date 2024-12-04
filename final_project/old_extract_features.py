import os
import pandas as pd
import csv

folder_path = "formatted_data"
output = 'features.csv'

with open(output, 'w', newline='') as csvfile:
    writer = csv.writer(csvfile)
    #writer.writerow(['Mean_X', 'Std_X', 'Mean_Y', 'Std_Y', 'Mean_Z', 'Std_Z', 'Activity'])
    writer.writerow(['x', 'y', 'z', 'Activity'])
    # Iterate over files in the folder
    for filename in os.listdir(folder_path):
        print(filename)
        file_path = os.path.join(folder_path, filename)
        if os.path.isfile(file_path):
            df = pd.read_csv(file_path)

            df.columns = ['Timestamp', 'X', 'Y', 'Z']

            #df['Timestamp'] = df['Timestamp'] // 1000

            # Group by each second based on Timestamp
            #grouped = df.groupby('Timestamp')
            row_ct = 0
            # Iterate over each group
            for index, row in df.iterrows():
                #print("group")
                # Skip the last group if it is incomplete
                #if timestamp == df['Timestamp'].max() and len(group) < 1000:
                 #   continue

                '''if len(group) > 0:
                    window_mean = group[['X', 'Y', 'Z']].mean()
                    window_std = group[['X', 'Y', 'Z']].std()
                    
                    window_mean = window_mean.fillna(0)
                    window_std = window_std.fillna(0)'''

                new_row = [
                        row['X'], row['Y'], row['Z'],
                        'fall' if "Activity 1" in filename else 'no_fall'
                ]
                row_ct += 1
                #print("writing new line")
                writer.writerow(new_row)
            print(row_ct)
