import requests
import json

BASE_URL = "http://localhost:8080/api/v1/auth"

def test_auth():
    print("--- Testing Registration ---")
    reg_data = {
        "username": "tester_" + str(hash("user") % 10000),
        "password": "password123",
        "email": "tester@test.com"
    }
    try:
        r = requests.post(f"{BASE_URL}/register", json=reg_data)
        print(f"Status Code: {r.status_code}")
        print(f"Response: {r.text}")
        
        if r.status_code == 200:
            print("\n--- Testing Login ---")
            login_data = {
                "username": reg_data["username"],
                "password": reg_data["password"]
            }
            rl = requests.post(f"{BASE_URL}/login", json=login_data)
            print(f"Status Code: {rl.status_code}")
            print(f"Response: {rl.text}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_auth()
