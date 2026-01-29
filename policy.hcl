# Allow the CI/CD pipeline to read payment gateway secrets
path "secret/data/payment-gateway/*" {
  capabilities = ["read"]
}

# Allow the pipeline to list secret paths (useful for debugging)
path "secret/metadata/payment-gateway/" {
  capabilities = ["list"]
}

# Deny everything else (Least Privilege)
path "secret/*" {
  capabilities = ["deny"]
}
