#!/bin/bash

# ============================================================================
# Inventory Service REST API Test Script
# ============================================================================
# This script tests all inventory endpoints including:
# - Basic CRUD operations
# - MicroProfile Rest Client integration
# - RestClientBuilder functionality
# - Product validation features
# - Reservation and advanced features
# ============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base URLs
INVENTORY_BASE_URL="http://localhost:7050/inventory/api"
CATALOG_BASE_URL="http://localhost:5050/catalog/api"

# Function to print section headers
print_section() {
    echo -e "\n${BLUE}============================================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================================================${NC}\n"
}

# Function to print test results
print_test() {
    echo -e "${YELLOW}TEST:${NC} $1"
    echo -e "${YELLOW}COMMAND:${NC} $2"
}

# Function to check if services are running
check_services() {
    print_section "🔍 CHECKING SERVICE AVAILABILITY"
    
    echo "Checking Catalog Service (port 5050)..."
    if curl -s "$CATALOG_BASE_URL/products" > /dev/null; then
        echo -e "${GREEN}✅ Catalog Service is running${NC}"
    else
        echo -e "${RED}❌ Catalog Service is not available${NC}"
        exit 1
    fi
    
    echo "Checking Inventory Service (port 7050)..."
    if curl -s "$INVENTORY_BASE_URL/inventories" > /dev/null; then
        echo -e "${GREEN}✅ Inventory Service is running${NC}"
    else
        echo -e "${RED}❌ Inventory Service is not available${NC}"
        exit 1
    fi
}

# Function to show available products in catalog
show_catalog_products() {
    print_section "📋 AVAILABLE PRODUCTS IN CATALOG"
    
    print_test "Get all products from catalog" "curl -X GET '$CATALOG_BASE_URL/products'"
    curl -X GET "$CATALOG_BASE_URL/products" -H "Content-Type: application/json" | jq '.'
    echo
}

# Test basic inventory operations
test_basic_operations() {
    print_section "🏪 BASIC INVENTORY OPERATIONS"
    
    # Get all inventories (should be empty initially)
    print_test "Get all inventories (empty initially)" "curl -X GET '$INVENTORY_BASE_URL/inventories'"
    curl -X GET "$INVENTORY_BASE_URL/inventories" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Create inventory for product 1 (iPhone)
    print_test "Create inventory for product 1 (iPhone)" "curl -X POST '$INVENTORY_BASE_URL/inventories'"
    curl -X POST "$INVENTORY_BASE_URL/inventories" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 1,
            "quantity": 100,
            "reservedQuantity": 0
        }' | jq '.'
    echo -e "\n"
    
    # Create inventory for product 2 (MacBook)
    print_test "Create inventory for product 2 (MacBook)" "curl -X POST '$INVENTORY_BASE_URL/inventories'"
    curl -X POST "$INVENTORY_BASE_URL/inventories" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 2,
            "quantity": 50,
            "reservedQuantity": 0
        }' | jq '.'
    echo -e "\n"
    
    # Create inventory for product 3 (iPad)
    print_test "Create inventory for product 3 (iPad)" "curl -X POST '$INVENTORY_BASE_URL/inventories'"
    curl -X POST "$INVENTORY_BASE_URL/inventories" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 3,
            "quantity": 75,
            "reservedQuantity": 0
        }' | jq '.'
    echo -e "\n"
    
    # Get all inventories after creation
    print_test "Get all inventories after creation" "curl -X GET '$INVENTORY_BASE_URL/inventories'"
    curl -X GET "$INVENTORY_BASE_URL/inventories" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Get inventory by ID
    print_test "Get inventory by ID (1)" "curl -X GET '$INVENTORY_BASE_URL/inventories/1'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/1" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Get inventory by product ID
    print_test "Get inventory by product ID (2)" "curl -X GET '$INVENTORY_BASE_URL/inventories/product/2'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/product/2" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test error handling
test_error_handling() {
    print_section "❌ ERROR HANDLING TESTS"
    
    # Try to create inventory for non-existent product
    print_test "Try to create inventory for non-existent product (999)" "curl -X POST '$INVENTORY_BASE_URL/inventories'"
    curl -X POST "$INVENTORY_BASE_URL/inventories" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 999,
            "quantity": 10,
            "reservedQuantity": 0
        }' | jq '.'
    echo -e "\n"
    
    # Try to create duplicate inventory
    print_test "Try to create duplicate inventory (product 1)" "curl -X POST '$INVENTORY_BASE_URL/inventories'"
    curl -X POST "$INVENTORY_BASE_URL/inventories" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 1,
            "quantity": 20,
            "reservedQuantity": 0
        }' | jq '.'
    echo -e "\n"
    
    # Try to get non-existent inventory
    print_test "Try to get non-existent inventory (999)" "curl -X GET '$INVENTORY_BASE_URL/inventories/999'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/999" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Try to get inventory for non-existent product
    print_test "Try to get inventory for non-existent product (888)" "curl -X GET '$INVENTORY_BASE_URL/inventories/product/888'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/product/888" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test update operations
test_update_operations() {
    print_section "✏️ UPDATE OPERATIONS"
    
    # Update inventory quantity
    print_test "Update inventory ID 1" "curl -X PUT '$INVENTORY_BASE_URL/inventories/1'"
    curl -X PUT "$INVENTORY_BASE_URL/inventories/1" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 1,
            "quantity": 120,
            "reservedQuantity": 5
        }' | jq '.'
    echo -e "\n"
    
    # Update quantity for product
    print_test "Update quantity for product 2 to 60" "curl -X PATCH '$INVENTORY_BASE_URL/inventories/product/2/quantity/60'"
    curl -X PATCH "$INVENTORY_BASE_URL/inventories/product/2/quantity/60" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Verify updates
    print_test "Verify updates - Get all inventories" "curl -X GET '$INVENTORY_BASE_URL/inventories'"
    curl -X GET "$INVENTORY_BASE_URL/inventories" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test pagination and filtering
test_pagination_filtering() {
    print_section "📄 PAGINATION AND FILTERING"
    
    # Test pagination
    print_test "Get inventories with pagination (page=0, size=2)" "curl -X GET '$INVENTORY_BASE_URL/inventories?page=0&size=2'"
    curl -X GET "$INVENTORY_BASE_URL/inventories?page=0&size=2" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Test filtering by minimum quantity
    print_test "Filter inventories with minimum quantity 70" "curl -X GET '$INVENTORY_BASE_URL/inventories?minQuantity=70'"
    curl -X GET "$INVENTORY_BASE_URL/inventories?minQuantity=70" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Test filtering by quantity range
    print_test "Filter inventories with quantity range (50-100)" "curl -X GET '$INVENTORY_BASE_URL/inventories?minQuantity=50&maxQuantity=100'"
    curl -X GET "$INVENTORY_BASE_URL/inventories?minQuantity=50&maxQuantity=100" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Get count with filters
    print_test "Count inventories with minimum quantity 60" "curl -X GET '$INVENTORY_BASE_URL/inventories/count?minQuantity=60'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/count?minQuantity=60" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test RestClientBuilder functionality (Product Availability)
test_restclient_availability() {
    print_section "🔌 RESTCLIENTBUILDER - PRODUCT AVAILABILITY"
    
    # Reserve inventory (uses RestClientBuilder for availability check)
    print_test "Reserve 10 units of product 1 (uses RestClientBuilder)" "curl -X PATCH '$INVENTORY_BASE_URL/inventories/product/1/reserve/10'"
    curl -X PATCH "$INVENTORY_BASE_URL/inventories/product/1/reserve/10" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Reserve inventory for product 2
    print_test "Reserve 5 units of product 2" "curl -X PATCH '$INVENTORY_BASE_URL/inventories/product/2/reserve/5'"
    curl -X PATCH "$INVENTORY_BASE_URL/inventories/product/2/reserve/5" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Try to reserve inventory for non-existent product
    print_test "Try to reserve inventory for non-existent product (999)" "curl -X PATCH '$INVENTORY_BASE_URL/inventories/product/999/reserve/1'"
    curl -X PATCH "$INVENTORY_BASE_URL/inventories/product/999/reserve/1" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Try to reserve more than available
    print_test "Try to reserve more than available (1000 units of product 3)" "curl -X PATCH '$INVENTORY_BASE_URL/inventories/product/3/reserve/1000'"
    curl -X PATCH "$INVENTORY_BASE_URL/inventories/product/3/reserve/1000" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test Advanced RestClientBuilder functionality
test_advanced_restclient() {
    print_section "🚀 ADVANCED RESTCLIENTBUILDER - CUSTOM CONFIGURATION"
    
    # Get product info using custom RestClientBuilder (3s connect, 8s read timeout)
    print_test "Get product 1 info using custom RestClientBuilder" "curl -X GET '$INVENTORY_BASE_URL/inventories/product-info/1'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/product-info/1" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Get product info for MacBook
    print_test "Get product 2 info using custom RestClientBuilder" "curl -X GET '$INVENTORY_BASE_URL/inventories/product-info/2'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/product-info/2" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Get product info for iPad
    print_test "Get product 3 info using custom RestClientBuilder" "curl -X GET '$INVENTORY_BASE_URL/inventories/product-info/3'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/product-info/3" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Try to get info for non-existent product
    print_test "Try to get info for non-existent product (777)" "curl -X GET '$INVENTORY_BASE_URL/inventories/product-info/777'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/product-info/777" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test enriched inventory with product information
test_enriched_inventory() {
    print_section "💎 ENRICHED INVENTORY WITH PRODUCT INFO"
    
    # Get inventory with product info by inventory ID
    print_test "Get inventory 1 with product information" "curl -X GET '$INVENTORY_BASE_URL/inventories/1/with-product-info'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/1/with-product-info" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Get inventories by category (if catalog supports categories)
    print_test "Get inventories by category 'electronics'" "curl -X GET '$INVENTORY_BASE_URL/inventories/category/electronics'"
    curl -X GET "$INVENTORY_BASE_URL/inventories/category/electronics" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test bulk operations
test_bulk_operations() {
    print_section "📦 BULK OPERATIONS"
    
    # Delete existing inventories first to test bulk create
    print_test "Delete inventory 1" "curl -X DELETE '$INVENTORY_BASE_URL/inventories/1'"
    curl -X DELETE "$INVENTORY_BASE_URL/inventories/1" -H "Content-Type: application/json"
    echo -e "\n"
    
    print_test "Delete inventory 2" "curl -X DELETE '$INVENTORY_BASE_URL/inventories/2'"
    curl -X DELETE "$INVENTORY_BASE_URL/inventories/2" -H "Content-Type: application/json"
    echo -e "\n"
    
    print_test "Delete inventory 3" "curl -X DELETE '$INVENTORY_BASE_URL/inventories/3'"
    curl -X DELETE "$INVENTORY_BASE_URL/inventories/3" -H "Content-Type: application/json"
    echo -e "\n"
    
    # Bulk create inventories
    print_test "Bulk create inventories" "curl -X POST '$INVENTORY_BASE_URL/inventories/bulk'"
    curl -X POST "$INVENTORY_BASE_URL/inventories/bulk" \
        -H "Content-Type: application/json" \
        -d '[
            {
                "productId": 1,
                "quantity": 200,
                "reservedQuantity": 0
            },
            {
                "productId": 2,
                "quantity": 150,
                "reservedQuantity": 0
            },
            {
                "productId": 3,
                "quantity": 100,
                "reservedQuantity": 0
            }
        ]' | jq '.'
    echo -e "\n"
    
    # Verify bulk creation
    print_test "Verify bulk creation - Get all inventories" "curl -X GET '$INVENTORY_BASE_URL/inventories'"
    curl -X GET "$INVENTORY_BASE_URL/inventories" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Test delete operations
test_delete_operations() {
    print_section "🗑️ DELETE OPERATIONS"
    
    # Delete inventory by ID
    print_test "Delete inventory by ID (2)" "curl -X DELETE '$INVENTORY_BASE_URL/inventories/5'"
    curl -X DELETE "$INVENTORY_BASE_URL/inventories/5" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Try to delete non-existent inventory
    print_test "Try to delete non-existent inventory (999)" "curl -X DELETE '$INVENTORY_BASE_URL/inventories/999'"
    curl -X DELETE "$INVENTORY_BASE_URL/inventories/999" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
    
    # Final inventory state
    print_test "Final inventory state" "curl -X GET '$INVENTORY_BASE_URL/inventories'"
    curl -X GET "$INVENTORY_BASE_URL/inventories" -H "Content-Type: application/json" | jq '.'
    echo -e "\n"
}

# Performance test
test_performance() {
    print_section "⚡ PERFORMANCE TESTS"
    
    echo "Testing response times for different RestClient approaches:"
    echo
    
    echo "1. Injected REST Client (used in product validation):"
    time curl -s -X POST "$INVENTORY_BASE_URL/inventories" \
        -H "Content-Type: application/json" \
        -d '{
            "productId": 1,
            "quantity": 1,
            "reservedQuantity": 0
        }' > /dev/null 2>&1 || true
    echo
    
    echo "2. RestClientBuilder with 5s/10s timeout (availability check):"
    time curl -s -X PATCH "$INVENTORY_BASE_URL/inventories/product/1/reserve/1" > /dev/null 2>&1 || true
    echo
    
    echo "3. RestClientBuilder with 3s/8s timeout (product info):"
    time curl -s -X GET "$INVENTORY_BASE_URL/inventories/product-info/1" > /dev/null 2>&1 || true
    echo
}

# Main execution
main() {
    echo -e "${GREEN}🚀 Starting Inventory Service REST API Tests${NC}"
    echo -e "${GREEN}Date: $(date)${NC}\n"
    
    # Check if jq is available
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}❌ jq is required for JSON formatting. Please install jq first.${NC}"
        echo "To install jq: sudo apt-get install jq (Ubuntu/Debian) or brew install jq (macOS)"
        exit 1
    fi
    
    check_services
    show_catalog_products
    test_basic_operations
    test_error_handling
    test_update_operations
    test_pagination_filtering
    test_restclient_availability
    test_advanced_restclient
    test_enriched_inventory
    test_bulk_operations
    test_delete_operations
    test_performance
    
    print_section "✅ ALL TESTS COMPLETED"
    echo -e "${GREEN}🎉 Inventory Service REST API testing completed successfully!${NC}"
    echo -e "${GREEN}📊 Summary of features tested:${NC}"
    echo -e "  • Basic CRUD operations"
    echo -e "  • MicroProfile Rest Client integration"
    echo -e "  • RestClientBuilder with custom configuration"
    echo -e "  • Product validation and error handling"
    echo -e "  • Inventory reservation functionality"
    echo -e "  • Pagination and filtering"
    echo -e "  • Bulk operations"
    echo -e "  • Performance comparison"
    echo
}

# Handle script arguments
case "${1:-}" in
    --basic)
        check_services
        test_basic_operations
        ;;
    --restclient)
        check_services
        test_restclient_availability
        test_advanced_restclient
        ;;
    --performance)
        check_services
        test_performance
        ;;
    --help)
        echo "Usage: $0 [--basic|--restclient|--performance|--help]"
        echo "  --basic      Run only basic CRUD tests"
        echo "  --restclient Run only RestClient tests"
        echo "  --performance Run only performance tests"
        echo "  --help       Show this help message"
        echo "  (no args)    Run all tests"
        ;;
    *)
        main
        ;;
esac
