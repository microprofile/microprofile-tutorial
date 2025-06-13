#!/bin/bash
# filepath: /workspaces/microprofile-tutorial/code/chapter03/mp-ecomm-store/test-api.sh

# MicroProfile E-Commerce Store API Test Script
# This script tests all CRUD operations for the Product API

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:5050/mp-ecomm-store/api/products"
CONTENT_TYPE="Content-Type: application/json"

echo "=================================="
echo "MicroProfile E-Commerce Store API Test"
echo "=================================="
echo "Base URL: $BASE_URL"
echo "=================================="

# Function to print section headers
print_section() {
    echo ""
    echo "--- $1 ---"
}

# Function to pause between operations
pause() {
    echo "Press Enter to continue..."
    read -r
}

print_section "1. GET ALL PRODUCTS (Initial State)"
echo "Command: curl -i -X GET $BASE_URL"
curl -i -X GET "$BASE_URL"
echo ""
pause

print_section "2. GET PRODUCT BY ID (Existing Product)"
echo "Command: curl -i -X GET $BASE_URL/1"
curl -i -X GET "$BASE_URL/1"
echo ""
pause

print_section "3. CREATE NEW PRODUCT"
echo "Command: curl -i -X POST $BASE_URL -H \"$CONTENT_TYPE\" -d '{\"id\": 3, \"name\": \"AirPods\", \"description\": \"Apple AirPods Pro\", \"price\": 249.99}'"
curl -i -X POST "$BASE_URL" \
  -H "$CONTENT_TYPE" \
  -d '{"id": 3, "name": "AirPods", "description": "Apple AirPods Pro", "price": 249.99}'
echo ""
pause

print_section "4. GET ALL PRODUCTS (After Creation)"
echo "Command: curl -i -X GET $BASE_URL"
curl -i -X GET "$BASE_URL"
echo ""
pause

print_section "5. GET NEW PRODUCT BY ID"
echo "Command: curl -i -X GET $BASE_URL/3"
curl -i -X GET "$BASE_URL/3"
echo ""
pause

print_section "6. UPDATE EXISTING PRODUCT"
echo "Command: curl -i -X PUT $BASE_URL/1 -H \"$CONTENT_TYPE\" -d '{\"id\": 1, \"name\": \"iPhone Pro\", \"description\": \"Apple iPhone 15 Pro\", \"price\": 1199.99}'"
curl -i -X PUT "$BASE_URL/1" \
  -H "$CONTENT_TYPE" \
  -d '{"id": 1, "name": "iPhone Pro", "description": "Apple iPhone 15 Pro", "price": 1199.99}'
echo ""
pause

print_section "7. GET UPDATED PRODUCT"
echo "Command: curl -i -X GET $BASE_URL/1"
curl -i -X GET "$BASE_URL/1"
echo ""
pause

print_section "8. DELETE PRODUCT"
echo "Command: curl -i -X DELETE $BASE_URL/3"
curl -i -X DELETE "$BASE_URL/3"
echo ""
pause

print_section "9. GET ALL PRODUCTS (After Deletion)"
echo "Command: curl -i -X GET $BASE_URL"
curl -i -X GET "$BASE_URL"
echo ""
pause

print_section "10. TRY TO GET DELETED PRODUCT (Should return 404)"
echo "Command: curl -i -X GET $BASE_URL/3"
curl -i -X GET "$BASE_URL/3" || true
echo ""
pause

print_section "11. TRY TO GET NON-EXISTENT PRODUCT (Should return 404)"
echo "Command: curl -i -X GET $BASE_URL/999"
curl -i -X GET "$BASE_URL/999" || true
echo ""

echo ""
echo "=================================="
echo "API Testing Complete!"
echo "=================================="