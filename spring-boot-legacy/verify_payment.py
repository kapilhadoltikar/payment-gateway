import urllib.request
import json
import time
import sys
import random

MERCHANT_SERVICE_URL = "http://localhost:8083"
PAYMENT_SERVICE_URL = "http://localhost:8082"

def post(url, data):
    print(f"POST {url}") # Debug
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req) as response:
            body = response.read().decode('utf-8')
            print(f"Response: {body}") # Debug
            return json.loads(body)
    except urllib.error.HTTPError as e:
        print(f"HTTP Error {e.code}: {e.reason}")
        print(e.read().decode('utf-8'))
        return None
    except Exception as e:
        print(f"Error: {e}")
        return None

def verify():
    print("1. Creating Merchant...")
    rand_id = random.randint(1000, 9999)
    merchant_req = {
        "name": f"Integration Test Merchant {rand_id}",
        "email": f"integration_{rand_id}@test.com",
        "webhookUrl": "http://localhost:9999/webhook"
    }
    resp = post(f"{MERCHANT_SERVICE_URL}/merchants", merchant_req)
    if not resp or not resp.get('success'):
        print("Failed to create merchant")
        sys.exit(1)
    
    merchant_id = resp['data']['id']
    print(f"Merchant Created: {merchant_id}")

    # 2. Good Payment
    print("\n2. Testing Clean Payment (Low Risk)...")
    payment_req = {
        "merchantId": merchant_id,
        "amount": 50.0,
        "currency": "USD",
        "cardNumber": "4111111111111111",
        "expiryMonth": "12",
        "expiryYear": "2030",
        "cvv": "123",
        "cardHolderName": "Good User",
        "customerEmail": "good_user@example.com",
        "paymentMethod": "CARD",
        "description": "Clean Transaction"
    }
    
    resp = post(f"{PAYMENT_SERVICE_URL}/payments/process", payment_req)
    if resp and resp.get('data') and resp['data'].get('status') == 'AUTHORIZED':
        print("SUCCESS: Payment Authorized")
    else:
        print(f"FAILURE: Payment not authorized. Response: {resp}")
        sys.exit(1)

    # 3. Fraud Payment
    print("\n3. Testing Fraud Payment (High Risk - Cold Start > $200)...")
    fraud_req = payment_req.copy()
    fraud_req['amount'] = 300.0
    fraud_req['customerEmail'] = "new_fraudster@example.com"
    fraud_req['description'] = "Fraud Transaction"

    resp = post(f"{PAYMENT_SERVICE_URL}/payments/process", fraud_req)
    # Check for FAILED status and failureReason
    if resp and resp.get('data') and resp['data'].get('status') == 'FAILED':
        reason = resp['data'].get('failureReason', '')
        if "Fraud" in reason:
             print(f"SUCCESS: Payment Blocked as Expected. Reason: {reason}")
        else:
             print(f"FAILURE: Payment Failed but unexpected reason: {reason}")
    else:
        print(f"FAILURE: Payment should have been blocked. Response: {resp}")
        sys.exit(1)

    print("\nVerification Complete: All tests passed.")

if __name__ == "__main__":
    verify()
