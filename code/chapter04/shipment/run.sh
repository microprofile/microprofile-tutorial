#!/bin/bash

# Build and run the Shipment Service
echo "Building and starting Shipment Service..."

# Stop running server if already running
if [ -f target/liberty/wlp/usr/servers/shipmentServer/workarea/.sRunning ]; then
    mvn liberty:stop
fi

# Clean, build and run
mvn clean package liberty:run
