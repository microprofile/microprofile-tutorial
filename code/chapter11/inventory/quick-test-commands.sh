#!/bin/bash

# Quick Inventory Service API Test Script
# Simple curl commands for manual testing

BASE_URL="http://localhost:7050/inventory/api"

echo "=== QUICK INVENTORY API TESTS ==="
echo

echo "1. Get all inventories:"
echo "curl -X GET '$BASE_URL/inventories' | jq"
echo

echo "2. Create inventory for product 1:"
echo "curl -X POST '$BASE_URL/inventories' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"productId\": 1, \"quantity\": 100, \"reservedQuantity\": 0}' | jq"
echo

echo "3. Get inventory by product ID:"
echo "curl -X GET '$BASE_URL/inventories/product/1' | jq"
echo

echo "4. Reserve inventory (RestClientBuilder availability check):"
echo "curl -X PATCH '$BASE_URL/inventories/product/1/reserve/10' | jq"
echo

echo "5. Get product info (Advanced RestClientBuilder):"
echo "curl -X GET '$BASE_URL/inventories/product-info/1' | jq"
echo

echo "6. Update inventory:"
echo "curl -X PUT '$BASE_URL/inventories/1' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"productId\": 1, \"quantity\": 120, \"reservedQuantity\": 5}' | jq"
echo

echo "7. Get inventory with product info:"
echo "curl -X GET '$BASE_URL/inventories/1/with-product-info' | jq"
echo

echo "8. Filter inventories by quantity:"
echo "curl -X GET '$BASE_URL/inventories?minQuantity=50&maxQuantity=150' | jq"
echo

echo "9. Bulk create inventories:"
echo "curl -X POST '$BASE_URL/inventories/bulk' \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '[{\"productId\": 2, \"quantity\": 50}, {\"productId\": 3, \"quantity\": 75}]' | jq"
echo

echo "10. Delete inventory:"
echo "curl -X DELETE '$BASE_URL/inventories/1' | jq"
echo

echo "=== MicroProfile Rest Client Features ==="
echo "• Product validation using @RestClient injection"
echo "• RestClientBuilder with custom timeouts (5s/10s for availability)"
echo "• Advanced RestClientBuilder with different timeouts (3s/8s for product info)"
echo "• Error handling for non-existent products"
echo "• Integration with catalog service on port 5050"
