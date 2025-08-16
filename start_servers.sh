#!/bin/bash

# GeoRDFBench UI System Startup Script

echo "🚀 Starting GeoRDFBench UI System..."

# Kill any existing processes on these ports
echo "🔄 Cleaning up existing processes..."
lsof -ti:5000 | xargs kill -9 2>/dev/null || true
lsof -ti:5001 | xargs kill -9 2>/dev/null || true

# Wait a moment for cleanup
sleep 2

# Start Endpoint Server
echo "📡 Starting Endpoint Server on port 5000..."
cd JsonGenerator/endpoint-server

# Start endpoint server in background with all required environment variables
PORT=5000 \
JSON_LIB_PATH="../../json_defs" \
NODE_ENV=development \
CRUD_MAP='{"datasets":"CRUD","executionspecs":"CRUD","hosts":"CRUD","querysets":"CRUD","reportsources":"CRUD","reportspecs":"CRUD","workloads":"CRUD"}' \
H2_JAR="h2-2.3.232.jar" \
H2_BASEDIR="./db" \
H2_HOST="localhost" \
H2_PORT="9092" \
H2_DBNAME="endpoint" \
H2_USER="sa" \
H2_PWD="" \
H2_WEB_PORT="8082" \
ACCESS_TOKEN_SECRET="dev-secret" \
REFRESH_TOKEN_SECRET="dev-refresh-secret" \
node endpoint.js &

ENDPOINT_PID=$!
echo "✅ Endpoint Server started with PID: $ENDPOINT_PID"

# Wait for endpoint server to start
sleep 3

# Start UI Server
echo "🎨 Starting UI Server on port 5001..."
cd ../jsongen-ui-server

# Start UI server in background with proper environment variables
PORT=5001 \
ENDPOINT_HOST=localhost \
ENDPOINT_PORT=5000 \
NODE_ENV=development \
ACCESS_TOKEN_SECRET="dev-secret" \
REFRESH_TOKEN_SECRET="dev-refresh-secret" \
node jsongen-ui-server.js &

UI_PID=$!
echo "✅ UI Server started with PID: $UI_PID"

# Wait for UI server to start
sleep 3

echo ""
echo "🎉 GeoRDFBench UI System is now running!"
echo ""
echo "📊 Access Points:"
echo "   • UI Server:       http://localhost:5001"
echo "   • Endpoint Server: http://localhost:5000"
echo "   • API Docs:        http://localhost:5000/api-docs"
echo ""
echo "🔧 Process IDs:"
echo "   • Endpoint Server PID: $ENDPOINT_PID"
echo "   • UI Server PID:       $UI_PID"
echo ""
echo "⏹️  To stop the servers, run:"
echo "   kill $ENDPOINT_PID $UI_PID"
echo ""
echo "📝 Logs will appear below..."
echo "----------------------------------------"

# Wait for both processes
wait
