"""
End-to-End Payment Flow Tests for Banking Gateway
"""
import pytest
import requests
import time


class TestPaymentFlow:
    """Test complete payment processing flows"""
    
    def test_successful_payment(self, api_base_url, headers, valid_payment_request):
        """Test successful payment processing"""
        response = requests.post(
            f"{api_base_url}/api/payments",
            json=valid_payment_request,
            headers=headers,
            timeout=15
        )
        
        assert response.status_code in [200, 201], f"Payment failed: {response.text}"
        
        data = response.json()
        assert "transactionId" in data or "id" in data
        assert data.get("status") in ["SUCCESS", "APPROVED", "COMPLETED"]
        
        print(f"✅ Payment processed: {data}")
    
    def test_payment_idempotency(self, api_base_url, headers, valid_payment_request):
        """Test payment idempotency - same request should not create duplicate charges"""
        idempotency_key = f"test-{int(time.time())}"
        headers_with_key = {
            **headers,
            "Idempotency-Key": idempotency_key
        }
        
        # First request
        response1 = requests.post(
            f"{api_base_url}/api/payments",
            json=valid_payment_request,
            headers=headers_with_key,
            timeout=15
        )
        
        # Second request with same idempotency key
        response2 = requests.post(
            f"{api_base_url}/api/payments",
            json=valid_payment_request,
            headers=headers_with_key,
            timeout=15
        )
        
        assert response1.status_code in [200, 201]
        assert response2.status_code in [200, 201]
        
        # Should return same transaction
        data1 = response1.json()
        data2 = response2.json()
        
        tx_id1 = data1.get("transactionId") or data1.get("id")
        tx_id2 = data2.get("transactionId") or data2.get("id")
        
        assert tx_id1 == tx_id2, "Idempotency violation: different transactions created"
        
        print(f"✅ Idempotency verified: {tx_id1}")
    
    def test_invalid_card_number(self, api_base_url, headers, valid_payment_request):
        """Test payment with invalid card number"""
        invalid_request = {
            **valid_payment_request,
            "cardNumber": "1234567890123456"  # Invalid card
        }
        
        response = requests.post(
            f"{api_base_url}/api/payments",
            json=invalid_request,
            headers=headers,
            timeout=15
        )
        
        # Should reject invalid card
        assert response.status_code in [400, 422], "Invalid card should be rejected"
        
        print(f"✅ Invalid card rejected: {response.status_code}")
    
    def test_expired_card(self, api_base_url, headers, valid_payment_request):
        """Test payment with expired card"""
        expired_request = {
            **valid_payment_request,
            "expiryMonth": "01",
            "expiryYear": "2020"  # Expired
        }
        
        response = requests.post(
            f"{api_base_url}/api/payments",
            json=expired_request,
            headers=headers,
            timeout=15
        )
        
        # Should reject expired card
        assert response.status_code in [400, 422], "Expired card should be rejected"
        
        print(f"✅ Expired card rejected: {response.status_code}")
    
    def test_payment_retrieval(self, api_base_url, headers, valid_payment_request):
        """Test retrieving payment details"""
        # Create a payment
        create_response = requests.post(
            f"{api_base_url}/api/payments",
            json=valid_payment_request,
            headers=headers,
            timeout=15
        )
        
        assert create_response.status_code in [200, 201]
        
        payment_data = create_response.json()
        payment_id = payment_data.get("transactionId") or payment_data.get("id")
        
        # Retrieve the payment
        get_response = requests.get(
            f"{api_base_url}/api/payments/{payment_id}",
            headers=headers,
            timeout=10
        )
        
        assert get_response.status_code == 200, "Payment retrieval failed"
        
        retrieved_data = get_response.json()
        assert retrieved_data.get("id") == payment_id or retrieved_data.get("transactionId") == payment_id
        
        print(f"✅ Payment retrieved: {payment_id}")
    
    def test_payment_audit_trail(self, api_base_url, headers, valid_payment_request):
        """Test that payment creates proper audit trail"""
        response = requests.post(
            f"{api_base_url}/api/payments",
            json=valid_payment_request,
            headers=headers,
            timeout=15
        )
        
        assert response.status_code in [200, 201]
        
        data = response.json()
        
        # Verify audit fields are present
        assert "createdAt" in data or "timestamp" in data, "Missing timestamp in audit trail"
        
        print(f"✅ Audit trail verified")
