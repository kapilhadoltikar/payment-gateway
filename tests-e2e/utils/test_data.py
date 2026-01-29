"""
Test Data Generators for Banking Scenarios
"""
import random
import time
from typing import Dict, Any


def generate_valid_card() -> Dict[str, str]:
    """Generate a valid test card number using Luhn algorithm"""
    # Test Visa card numbers
    test_cards = [
        "4532015112830366",
        "4556737586899855",
        "4916338506082832",
        "4024007198964305"
    ]
    
    card = random.choice(test_cards)
    
    return {
        "cardNumber": card,
        "cardHolderName": random.choice([
            "John Doe",
            "Jane Smith",
            "Robert Johnson",
            "Emily Davis"
        ]),
        "expiryMonth": f"{random.randint(1, 12):02d}",
        "expiryYear": str(random.randint(2025, 2030)),
        "cvv": f"{random.randint(100, 999)}"
    }


def generate_payment_request(amount: float = None) -> Dict[str, Any]:
    """Generate a complete payment request"""
    card_data = generate_valid_card()
    
    return {
        **card_data,
        "amount": amount or round(random.uniform(10.0, 500.0), 2),
        "currency": "USD",
        "description": f"Test transaction {int(time.time())}"
    }


def generate_merchant_data() -> Dict[str, Any]:
    """Generate merchant registration data"""
    timestamp = int(time.time())
    
    return {
        "merchantName": f"Test Merchant {timestamp}",
        "email": f"merchant-{timestamp}@test.com",
        "password": "SecurePass123!",
        "businessType": random.choice(["RETAIL", "ECOMMERCE", "BANKING", "FINTECH"])
    }


def generate_high_risk_payment() -> Dict[str, Any]:
    """Generate a high-risk payment request for fraud testing"""
    card_data = generate_valid_card()
    
    return {
        **card_data,
        "amount": round(random.uniform(5000.0, 10000.0), 2),
        "currency": "USD",
        "description": "High-value transaction"
    }
