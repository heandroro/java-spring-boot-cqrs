#!/bin/bash
set -e

echo "🔧 Setting up test data for Orders Service..."

# Verificar se aplicação está rodando
echo "🔍 Checking application health..."
curl -f http://localhost:8080/actuator/health || {
    echo "❌ Application not running!"
    echo "💡 Start the application with: mvn spring-boot:run"
    exit 1
}

# Criar alguns pedidos iniciais para testes de GET
echo "📦 Creating initial orders..."

CUSTOMERS=(
    "550e8400-e29b-41d4-a716-446655440000"
    "550e8400-e29b-41d4-a716-446655440001"
    "550e8400-e29b-41d4-a716-446655440002"
)

PRODUCTS=("p001" "p002" "p003" "p004" "p005")
PRICES=("9.99" "19.99" "29.99" "39.99" "49.99")

CREATED_COUNT=0

for i in {1..10}; do
    CUSTOMER_IDX=$((RANDOM % ${#CUSTOMERS[@]}))
    PRODUCT_IDX=$((RANDOM % ${#PRODUCTS[@]}))
    QUANTITY=$((RANDOM % 5 + 1))
    
    CUSTOMER_ID=${CUSTOMERS[$CUSTOMER_IDX]}
    PRODUCT_ID=${PRODUCTS[$PRODUCT_IDX]}
    PRICE=${PRICES[$PRODUCT_IDX]}
    
    PAYLOAD=$(cat <<EOF
{
  "customerId": "$CUSTOMER_ID",
  "items": [
    {
      "productId": "$PRODUCT_ID",
      "quantity": $QUANTITY,
      "pricePerUnit": $PRICE
    }
  ]
}
EOF
)
    
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/orders \
        -H "Content-Type: application/json" \
        -d "$PAYLOAD")
    
    HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
    
    if [ "$HTTP_CODE" = "201" ]; then
        CREATED_COUNT=$((CREATED_COUNT + 1))
        echo "✅ Created order $CREATED_COUNT/10"
    else
        echo "⚠️  Failed to create order (HTTP $HTTP_CODE)"
    fi
    
    sleep 0.1
done

echo ""
echo "✅ Setup completed!"
echo "📊 Created $CREATED_COUNT orders"
echo "🚀 Ready to run stress tests!"
