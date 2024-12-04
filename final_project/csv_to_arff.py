import os
import csv

def filter_sensor_file(file_path, sensor_id):
    filtered_data = []
    with open(file_path, 'r') as file:
        lines = file.readlines()
        for line in lines[1:]:
            tokens = line.strip().split(',')
            if len(tokens) > 1 and int(tokens[1]) == sensor_id:
                timestamp = int(tokens[0])
                values = tokens[3:]
                filtered_line = [str(timestamp)] + values
                filtered_data.append(filtered_line)
    return filtered_data

def filter_all_sensor_files(src_folder, dest_folder, sensor_dict):
    for sensor_id, sensor_name in sensor_dict.items():
        sensor_dest_folder = os.path.join(dest_folder, sensor_name)
        if not os.path.exists(sensor_dest_folder):
            os.makedirs(sensor_dest_folder)
        
        for file_name in os.listdir(src_folder):
            if file_name.endswith(".csv"):
                file_path = os.path.join(src_folder, file_name)
                filtered_data = filter_sensor_file(file_path, sensor_id)
                
                dest_file_path = os.path.join(sensor_dest_folder, file_name)
                with open(dest_file_path, 'w', newline='') as csvfile:
                    writer = csv.writer(csvfile)
                    writer.writerows(filtered_data)

                print(f"Processed and saved: {dest_file_path}")

filter_all_sensor_files("raw_data", "formatted_data", {1: "Accelerometer Data", 4: "Gryoscope Data"})
