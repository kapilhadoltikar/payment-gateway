"""
API Client Utilities for E2E Tests
"""
import requests
from typing import Dict, Any, Optional


class PaymentGatewayClient:
    """Client for interacting with Payment Gateway API"""
    
    def __init__(self, base_url: str, token: Optional[str] = None):
        self.base_url = base_url
        self.token = token
    
    @property
    def headers(self) -> Dict[str, str]:
        """Get request headers with authentication"""
        headers = {"Content-Type": "application/json"}
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"
        return headers
    
    def create_payment(self, payment_data: Dict[str, Any]) -> requests.Response:
        """Create a payment transaction"""
        return requests.post(
            f"{self.base_url}/api/payments",
            json=payment_data,
            headers=self.headers,
            timeout=15
        )
    
    def get_payment(self, payment_id: str) -> requests.Response:
        """Retrieve payment details"""
        return requests.get(
            f"{self.base_url}/api/payments/{payment_id}",
            headers=self.headers,
            timeout=10
        )
    
    def register_merchant(self, merchant_data: Dict[str, Any]) -> requests.Response:
        """Register a new merchant"""
        return requests.post(
            f"{self.base_url}/api/auth/register",
            json=merchant_data,
            timeout=10
        )
    
    def login(self, email: str, password: str) -> requests.Response:
        """Login and get auth token"""
        return requests.post(
            f"{self.base_url}/api/auth/login",
            json={"email": email, "password": password},
            timeout=10
        )
