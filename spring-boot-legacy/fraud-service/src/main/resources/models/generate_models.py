import numpy as np
from sklearn.datasets import make_classification
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
import xgboost as xgb
import onnxmltools
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import os
import json

# Create models directory if not exists
if not os.path.exists("models"):
    os.makedirs("models")

print("Generating dummy fraud data...")
# Generate dummy data (11 features as per Java code)
X, y = make_classification(n_samples=1000, n_features=11, n_informative=5, n_redundant=2, random_state=42)
X = X.astype(np.float32)

# --- 0. Scaling (CRITICAL STEP) ---
print("Fitting StandardScaler...")
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# Export Scaler Parameters for Java
scaler_params = {
    "mean": scaler.mean_.tolist(),
    "scale": scaler.scale_.tolist()
}
with open("models/scaler_params.json", "w") as f:
    json.dump(scaler_params, f)
print("Saved models/scaler_params.json")

# --- 1. Train Logistic Regression (For Reference/Export if needed) ---
print("Training Logistic Regression...")
lr = LogisticRegression(class_weight='balanced') # Added class_weight
lr.fit(X_scaled, y) # Train on SCALED data

# Export LR to ONNX
initial_type = [('input', FloatTensorType([None, 11]))]
onnx_lr = convert_sklearn(lr, initial_types=initial_type)
with open("models/logistic_regression.onnx", "wb") as f:
    f.write(onnx_lr.SerializeToString())
print("Saved models/logistic_regression.onnx")

# --- 2. Train XGBoost (Tier 2) ---
print("Training XGBoost...")
# Calculate scale_pos_weight
num_neg = (y == 0).sum()
num_pos = (y == 1).sum()
scale_pos_weight = num_neg / num_pos

xgb_model = xgb.XGBClassifier(
    n_estimators=50, 
    max_depth=4, 
    learning_rate=0.1, 
    scale_pos_weight=scale_pos_weight
)
xgb_model.fit(X_scaled, y) # Train on SCALED data

# Export XGBoost to ONNX
print("Converting XGBoost to ONNX...")
from onnxmltools.convert import convert_xgboost
from onnxmltools.convert.common.data_types import FloatTensorType

initial_type = [('input', FloatTensorType([None, 11]))]
onnx_xgb = convert_xgboost(xgb_model, initial_types=initial_type)

with open("models/xgboost_fraud.onnx", "wb") as f:
    f.write(onnx_xgb.SerializeToString())
print("Saved models/xgboost_fraud.onnx")

print("Done! You can now restart the fraud-service.")
