FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi

# Copy configuration files
COPY --chown=1001:0 src/main/liberty/config/ /config/

# Create the apps directory and copy the application
COPY --chown=1001:0 target/shoppingcart.war /config/apps/

# Configure the server to run in production mode
RUN configure.sh

# Expose the default port
EXPOSE 4050 4443

# Set the health check
HEALTHCHECK --start-period=60s --interval=10s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:4050/health || exit 1

# Run the server
CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
