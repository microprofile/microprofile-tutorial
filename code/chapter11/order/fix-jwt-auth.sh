#!/bin/bash
# Script to fix JWT authentication issues in the order service

set -e  # Exit on any error

echo "=== Order Service JWT Authentication Fix ==="

cd /workspaces/liberty-rest-app/order

# 1. Stop the server if running
echo "Stopping Liberty server..."
mvn liberty:stop || true

# 2. Clean the target directory to ensure clean configuration
echo "Cleaning target directory..."
mvn clean

# 3. Make sure our public key is in the correct format
echo "Checking public key format..."
cat src/main/resources/META-INF/publicKey.pem
if ! grep -q "BEGIN PUBLIC KEY" src/main/resources/META-INF/publicKey.pem; then
    echo "ERROR: Public key is not in the correct format. It should start with '-----BEGIN PUBLIC KEY-----'"
    exit 1
fi

# 4. Verify microprofile-config.properties
echo "Checking microprofile-config.properties..."
cat src/main/resources/META-INF/microprofile-config.properties
if ! grep -q "mp.jwt.verify.publickey.location" src/main/resources/META-INF/microprofile-config.properties; then
    echo "ERROR: mp.jwt.verify.publickey.location not found in microprofile-config.properties"
    exit 1
fi

# 5. Verify web.xml
echo "Checking web.xml..."
cat src/main/webapp/WEB-INF/web.xml | grep -A 3 "login-config"
if ! grep -q "<auth-method>MP-JWT</auth-method>" src/main/webapp/WEB-INF/web.xml; then
    echo "ERROR: MP-JWT auth-method not found in web.xml"
    exit 1
fi

# 6. Create the configuration directory and copy resources
echo "Creating configuration directory structure..."
mkdir -p target/liberty/wlp/usr/servers/orderServer

# 7. Copy the public key to the Liberty server configuration directory
echo "Copying public key to Liberty server configuration..."
cp src/main/resources/META-INF/publicKey.pem target/liberty/wlp/usr/servers/orderServer/

# 8. Build the application with the updated configuration
echo "Building and packaging the application..."
mvn package

# 9. Start the server with the fixed configuration
echo "Starting the Liberty server..."
mvn liberty:start

# 10. Verify the server started properly
echo "Verifying server status..."
sleep 5  # Give the server a moment to start
if grep -q "CWWKF0011I" target/liberty/wlp/usr/servers/orderServer/logs/messages.log; then
    echo "✅ Server started successfully!"
else
    echo "❌ Server may have encountered issues. Check logs for details."
fi

echo "=== Fix applied successfully! ==="
echo "Now test JWT authentication with: ./debug-jwt.sh"
