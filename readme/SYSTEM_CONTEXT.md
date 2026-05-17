# System Context Diagram

This diagram provides a high-level overview of how the **Payment Gateway** interacts with users and external third-party systems.

```mermaid
C4Context
    title "System Context Diagram: Payment Gateway"
    
    Person(merchant, "Merchant", "A business user using the gateway to process customer payments and manage their account.")
    Person(customer, "Customer", "The end-user who provides payment details on a merchant's checkout page.")
    
    System(gateway, "Payment Gateway System", "Handles secure payment processing, fraud detection, and transactional integrity ( GraalVM Native / JVM ).")

    System_Ext(bank, "Acquiring Bank / Processor", "External financial institution that clears and settles funds.")
    System_Ext(email, "Notification Service", "Third-party providers (SendGrid/Twilio) for sending transaction alerts.")

    Rel(customer, merchant, "Places order / Provides card details", "HTTPS")
    Rel(merchant, gateway, "Submits payment request / Manages API keys", "JSON/HTTPS")
    
    Rel_D(gateway, bank, "Authorizes & Settles transactions", "mTLS/API")
    Rel_D(gateway, email, "Dispatches transaction alerts", "SMTP/REST")
    
    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Key Interactions

1.  **Merchant to Gateway**: Merchants integrate via the **API Gateway** to process transactions.
2.  **Gateway to Bank**: The **Payment Service** communicates with external acquiring banks to authorize funds.
3.  **Security Boundary**: All card data is tokenized internally before being stored or transmitted.
4.  **Async Notifications**: The **Notification Service** handles delivery of receipts and webhooks to external providers.
