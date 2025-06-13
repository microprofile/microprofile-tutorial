#!/bin/bash
# Script to copy JWT public key to Liberty server config directory and restart server

LIBERTY_CONFIG_DIR="/workspaces/liberty-rest-app/order/target/liberty/wlp/usr/servers/orderServer"
PUBLIC_KEY_SOURCE="/workspaces/liberty-rest-app/order/src/main/resources/META-INF/publicKey.pem"

echo "Copying JWT public key to Liberty server config directory..."

# Make sure the Liberty server directory exists
if [ ! -d "$LIBERTY_CONFIG_DIR" ]; then
  echo "Liberty server directory doesn't exist yet. Building project first..."
  cd /workspaces/liberty-rest-app/order
  mvn clean package
fi

# Make sure the target directory exists
mkdir -p "$LIBERTY_CONFIG_DIR"

# Copy the public key
cp "$PUBLIC_KEY_SOURCE" "$LIBERTY_CONFIG_DIR/publicKey.pem"

# Verify the key was copied and has correct format
if [ -f "$LIBERTY_CONFIG_DIR/publicKey.pem" ]; then
  echo "Public key copied successfully."
  
  # Display key format to verify it's correct
  echo "Verifying key format..."
  head -1 "$LIBERTY_CONFIG_DIR/publicKey.pem"
  echo "..." 
  tail -1 "$LIBERTY_CONFIG_DIR/publicKey.pem"
else
  echo "Error: Failed to copy public key"
  exit 1
fi

# Restart the Liberty server
echo "Restarting Liberty server..."
cd /workspaces/liberty-rest-app/order
mvn liberty:stop
mvn liberty:start

echo "Liberty server restarted. JWT authentication should now work correctly."
echo "You can test it with: ./test-jwt.sh"
