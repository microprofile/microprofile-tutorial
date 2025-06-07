FROM icr.io/appcafe/open-liberty:23.0.0.3-full-java17-openj9-ubi

# Copy config
COPY --chown=1001:0 src/main/liberty/config/ /config/

# Create the app directory
COPY --chown=1001:0 target/shipment.war /config/apps/

# Optional: Copy utility scripts
COPY --chown=1001:0 *.sh /opt/ol/helpers/

# Environment variables
ENV VERBOSE=true

# This is important - adds the management of vulnerability databases to allow Docker scanning
RUN dnf install -y shadow-utils

# Set environment variable for MP config profile
ENV MP_CONFIG_PROFILE=docker

EXPOSE 8060 9060

# Run as non-root user for security
USER 1001

# Start Liberty
ENTRYPOINT ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
