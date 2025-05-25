#!/bin/bash

# Generate a secure JWT secret for Apollo microservices
# This script generates a base64-encoded 256-bit secret suitable for JWT signing

echo "🔐 Generating JWT Secret for Apollo Microservices"
echo "================================================"

# Generate 32 random bytes (256 bits) and encode as base64
JWT_SECRET=$(openssl rand -base64 32)

echo "Generated JWT Secret:"
echo "$JWT_SECRET"
echo ""
echo "Add this to your .env file:"
echo "JWT_SECRET=$JWT_SECRET"
echo ""
echo "⚠️  Keep this secret secure and never commit it to version control!" 