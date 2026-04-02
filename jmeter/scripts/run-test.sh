#!/bin/bash
set -e

TIMESTAMP=$(date +%Y%m%d-%H%M%S)
RESULTS_DIR="jmeter/results"
TEST_PLAN="jmeter/test-plans/orders-stress-test.jmx"

echo "🚀 Starting Orders Service Stress Test..."
echo "📅 Timestamp: $TIMESTAMP"

# Verificar aplicação
echo "🔍 Checking application health..."
curl -f http://localhost:8080/health || {
    echo "❌ Application not running!"
    echo "💡 Start the mock server with: java SimpleMockServer"
    exit 1
}

# Verificar se JMeter está instalado
if ! command -v jmeter &> /dev/null; then
    echo "❌ JMeter not found!"
    echo "💡 Install with: brew install jmeter"
    exit 1
fi

# Verificar se test plan existe
if [ ! -f "$TEST_PLAN" ]; then
    echo "❌ Test plan not found: $TEST_PLAN"
    echo "💡 Create the test plan using JMeter GUI first"
    exit 1
fi

# Executar JMeter
echo "⚡ Running JMeter test (5 minutes)..."
echo "📊 This will generate HTML report automatically..."

jmeter -n -t "$TEST_PLAN" \
       -l "$RESULTS_DIR/results-$TIMESTAMP.csv" \
       -e -o "$RESULTS_DIR/html-report-$TIMESTAMP" \
       -Jjmeter.save.saveservice.output_format=csv

echo ""
echo "✅ Test completed!"
echo "📊 HTML Report: $RESULTS_DIR/html-report-$TIMESTAMP/index.html"
echo "📄 CSV Results: $RESULTS_DIR/results-$TIMESTAMP.csv"
echo ""

# Abrir relatório
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "🌐 Opening report in browser..."
    open "$RESULTS_DIR/html-report-$TIMESTAMP/index.html"
else
    echo "🌐 Open the report manually: $RESULTS_DIR/html-report-$TIMESTAMP/index.html"
fi
