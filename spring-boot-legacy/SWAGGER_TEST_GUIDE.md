# Swagger Testing Guide

Follow these steps to test the Payment Gateway microservices manually using the built-in Swagger UI.

## 1. Prerequisites
Ensure the environment is running:
```powershell
# Start infrastructure
task docker:up

# Run all services
task run:local
```

## 2. Port Reference Table

| Service | Port | Swagger URL |
| :--- | :--- | :--- |
| **API Gateway** | 8080 | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| **Auth Service** | 8081 | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) |
| **Payment Service** | 8082 | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) |
| **Merchant Service** | 8083 | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) |
| **Vault Service** | 8084 | [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) |
| **Notification** | 8085 | [http://localhost:8085/swagger-ui/index.html](http://localhost:8085/swagger-ui/index.html) |
| **Fraud Service** | 8086 | [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html) |

---

## 3. Step 1: Get an Access Token
Most services require a valid JWT token.

1.  Navigate to the **Auth Service** Swagger: `http://localhost:8081/swagger-ui/index.html`
2.  Expand **POST `/auth/register`**.
3.  Click **Try it out**.
4.  Use this payload:
    ```json
    {
      "username": "tester",
      "password": "password123",
      "email": "tester@example.com",
      "roles": ["USER"]
    }
    ```
5.  Click **Execute**.
6.  Expand **POST `/auth/login`**.
7.  Login with the same username/password.
8.  **Copy the `token`** from the JSON response (e.g., `eyJhbGci...`).

---

## 4. Step 2: Authorize in Other Services
Before testing Payment or Merchant services, you must "Log in" to their Swagger UI.

1.  Open the desired service (e.g., **Payment Service** at port `8082`).
2.  Click the **Authorize** button (top right).
3.  In the `Value` box, type: `Bearer <YOUR_TOKEN>` (replace `<YOUR_TOKEN>` with the string from Step 1).
4.  Click **Authorize** then **Close**.

---

## 5. Step 3: Test Workflows

### Scenario A: Create a Merchant
1.  Go to **Merchant Service** (Port `8083`).
2.  **Authorize** (using the token from Step 1).
3.  Use **POST `/merchants`**:
    ```json
    {
      "name": "Test Shop",
      "email": "shop@example.com",
      "apiKey": "M123"
    }
    ```

### Scenario B: Process a CLEAN Payment (< $200)
1.  Go to **Payment Service** (Port `8082`).
2.  **Authorize**.
3.  Use **POST `/payments/process`**:
    ```json
    {
      "merchantId": "M123",
      "userId": "U1",
      "amount": 100.0,
      "currency": "USD",
      "cardDetails": {
        "pan": "4111222233334444",
        "expiryDate": "12/26",
        "cvv": "123",
        "cardHolderName": "John Doe"
      }
    }
    ```
4.  **Expect**: `status: "AUTHORIZED"`.

### Scenario C: Test Fraud Blocking (> $200)
1.  Use the same **POST `/payments/process`** but change `amount` to `250.0`.
2.  **Expect**: `status: "FAILED"` with reason `"High Risk Fraud Detected"`.

---

## Troubleshooting
- **403 Forbidden**: You forgot to click "Authorize" or didn't include "Bearer " prefix.
- **500 Internal Error**: Check if the database is up (`task docker:up`).
- **Connection Refused**: Check if the service is running (`task run:local`).
