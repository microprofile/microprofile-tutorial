#!/bin/bash

# Script to test the ProductClientJson.getProductsWithJsonp method integration

echo "=== Testing ProductClientJson Integration ==="

# Base URL for the payment service
PAYMENT_SERVICE_URL="http://localhost:9080/payment/api"

echo ""
echo "1. Testing Get All Products"
echo "curl -X GET $PAYMENT_SERVICE_URL/products"
curl -X GET "$PAYMENT_SERVICE_URL/products" | json_pp 2>/dev/null || echo "Failed to parse JSON response"

echo ""
echo "2. Testing Get Products from Custom URL"
echo "curl -X GET '$PAYMENT_SERVICE_URL/products/from-url?url=http://localhost:5050/catalog/api/products'"
curl -X GET "$PAYMENT_SERVICE_URL/products/from-url?url=http://localhost:5050/catalog/api/products" | json_pp 2>/dev/null || echo "Failed to parse JSON response"

echo ""
echo "3. Testing Product Validation"
echo "curl -X GET $PAYMENT_SERVICE_URL/products/1/validate"
curl -X GET "$PAYMENT_SERVICE_URL/products/1/validate" | json_pp 2>/dev/null || echo "Failed to parse JSON response"

echo ""
echo "4. Testing Products by Price Range"
echo "curl -X GET '$PAYMENT_SERVICE_URL/products/price-range?minPrice=10&maxPrice=100'"
curl -X GET "$PAYMENT_SERVICE_URL/products/price-range?minPrice=10&maxPrice=100" | json_pp 2>/dev/null || echo "Failed to parse JSON response"

echo ""
echo "5. Testing ProductClientExample (if compiled)"
echo "cd /workspaces/liberty-rest-app/payment && java -cp target/classes io.microprofile.tutorial.store.payment.examples.ProductClientExample"

echo ""
echo "=== Test Complete ==="
