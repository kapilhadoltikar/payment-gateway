# Placeholder for Logistic Regression ONNX Model

This file should contain the trained Logistic Regression model exported to ONNX format.

## Model Specifications
- **Input**: Float tensor [1, 5] - (amount, velocity, time_of_day, amount_delta, new_device)
- **Output**: Float tensor [1, 1] - Fraud probability [0.0-1.0]
- **Size**: ~150 KB
- **Inference Time**: ~0.8ms

## Training Script
```python
from sklearn.linear_model import LogisticRegression
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

# Train model
model = LogisticRegression()
model.fit(X_train, y_train)

# Convert to ONNX
initial_type = [('input', FloatTensorType([None, 5]))]
onnx_model = convert_sklearn(model, initial_types=initial_type)

# Save
with open("logistic_regression.onnx", "wb") as f:
    f.write(onnx_model.SerializeToString())
```

**Note**: Replace this placeholder with actual trained model before deployment.
