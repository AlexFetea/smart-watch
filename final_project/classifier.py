import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report

# Load the dataset
file_path = "features.csv"  # Update with the path to your CSV file
data = pd.read_csv(file_path)

# Assuming the CSV has columns 'Feature1', 'Feature2', ..., 'Feature6', and 'Label'
# Split features and label
features = data.iloc[:, :-1]  # All columns except the last one are features
labels = data.iloc[:, -1]     # The last column is the label

# Split data into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(features, labels, test_size=0.2, random_state=42)

# Develop the model (Random Forest Classifier)
model = RandomForestClassifier(random_state=42)
model.fit(X_train, y_train)

# Make predictions
y_pred = model.predict(X_test)

# Evaluate the model
accuracy = accuracy_score(y_test, y_pred)
print(y_pred)
print("Model Accuracy:", accuracy)
print("\nClassification Report:")
print(classification_report(y_test, y_pred))

