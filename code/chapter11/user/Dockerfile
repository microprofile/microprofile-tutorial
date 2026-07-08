FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi

# Copy Liberty configuration
COPY --chown=1001:0 src/main/liberty/config/ /config/

# Copy application WAR file
COPY --chown=1001:0 target/user.war /config/apps/

# Set environment variables
ENV PORT=9080

# Configure the server to run
RUN configure.sh

# Expose ports
EXPOSE 6050 6051

# Start the server
CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
