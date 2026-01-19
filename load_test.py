import urllib.request
import urllib.error
import json
import time
import threading
import random
import statistics
import sys
from concurrent.futures import ThreadPoolExecutor

# Direct Service URLs
AUTH_URL = "http://localhost:8081/auth"
MERCHANT_URL = "http://localhost:8083/merchants"
PAYMENT_URL = "http://localhost:8082/payments"

CONCURRENCY = 1  # Minimal concurrency to ensure stability
DURATION_SECONDS = 30 

success_count = 0
error_count = 0
latencies = []
lock = threading.Lock()

def make_request(url, method="POST", data=None, headers=None):
    if headers is None:
        headers = {}
    
    headers['Content-Type'] = 'application/json'
    
    req = urllib.request.Request(url, method=method)
    for k, v in headers.items():
        req.add_header(k, v)
    
    json_data = None
    if data:
        json_data = json.dumps(data).encode('utf-8')

    try:
        start_time = time.time()
        with urllib.request.urlopen(req, data=json_data) as response:
            body = response.read()
            latency = (time.time() - start_time) * 1000 # ms
            return response.status, json.loads(body), latency
    except urllib.error.HTTPError as e:
        print(f"HTTP Error {e.code}: {e.read().decode('utf-8')}")
        return e.code, None, 0
    except Exception as e:
        print(f"Request Error: {e}")
        return 500, None, 0

def get_auth_token():
    user_suffix = random.randint(1000, 9999)
    payload = {
        "username": f"loadtest_user_{user_suffix}",
        "password": "password123",
        "email": f"loadtest_{user_suffix}@example.com"
    }
    status, body, _ = make_request(f"{AUTH_URL}/register", data=payload)
    if status == 200 and 'data' in body:
        return body['data']['token']
    raise Exception(f"Failed to get auth token. Status: {status}")

def register_merchant(token):
    merchant_suffix = random.randint(1000, 9999)
    payload = {
        "name": f"LoadTest Merchant {merchant_suffix}",
        "email": f"merchant_{merchant_suffix}@test.com",
        "webhookUrl": "http://localhost:9999/webhook"
    }
    headers = {"Authorization": f"Bearer {token}"}
    status, body, _ = make_request(MERCHANT_URL, data=payload, headers=headers)
    if status == 200 and 'data' in body:
        print(f"DEBUG: Merchant Response: {body}")
        if 'id' in body['data']: return body['data']['id']
        elif 'merchantId' in body['data']: return body['data']['merchantId']
        else: return body['data'].get('id') # Fallback
    raise Exception(f"Failed to register merchant. Status: {status}, Body: {body}")

def process_payment(token, merchant_id):
    global success_count, error_count, latencies
    payload = {
        "merchantId": merchant_id,
        "amount": random.randint(10, 500),
        "currency": "USD",
        "paymentMethod": "CARD",
        "cardNumber": "4111222233334444",
        "expiryMonth": "12",
        "expiryYear": "2030",
        "cvv": "123",
        "cardHolderName": "Load Tester",
        "customerEmail": "tester@example.com"
    }
    headers = {"Authorization": f"Bearer {token}"}
    
    # Hit Payment Service process endpoint
    status, _, latency = make_request(f"{PAYMENT_URL}/process", data=payload, headers=headers)
    
    with lock:
        if status == 200:
            success_count += 1
            latencies.append(latency)
        else:
            error_count += 1

def run_load_test():
    print("--- Starting Payment Gateway Load Test (Direct Service Access) ---")
    
    try:
        print("1. Authenticating (Auth Service :8081)...")
        token = get_auth_token()
        print("   Token acquired.")
        
        print("2. Registering Merchant (Merchant Service :8083)...")
        merchant_id = register_merchant(token)
        print(f"   Merchant ID: {merchant_id}")
        
        print(f"3. Running Load Test against Payment Service (:8082)")
        print(f"   Concurrency: {CONCURRENCY}, Duration: {DURATION_SECONDS}s...")
        
        start_time = time.time()
        end_time = start_time + DURATION_SECONDS
        
        with ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
            while time.time() < end_time:
                futures = []
                # Over-provision tasks slightly to keep threads busy
                for _ in range(CONCURRENCY * 2):
                    if time.time() >= end_time: break
                    futures.append(executor.submit(process_payment, token, merchant_id))
                
                for f in futures:
                    f.result()
                    
        total_time = time.time() - start_time
        
        print("\n--- Results ---")
        total_reqs = success_count + error_count
        rps = total_reqs / total_time
        
        print(f"Total Requests: {total_reqs}")
        print(f"Successful:     {success_count}")
        print(f"Failed:         {error_count}")
        print(f"Duration:       {total_time:.2f} seconds")
        print(f"RPS:            {rps:.2f}")
        
        if latencies:
            print(f"Avg Latency:    {statistics.mean(latencies):.2f} ms")
            print(f"P50 Latency:    {statistics.median(latencies):.2f} ms")
            print(f"P95 Latency:    {statistics.quantiles(latencies, n=20)[18]:.2f} ms")
            print(f"P99 Latency:    {statistics.quantiles(latencies, n=100)[98]:.2f} ms")

    except Exception as e:
        print(f"Test Aborted: {e}")

if __name__ == "__main__":
    run_load_test()
