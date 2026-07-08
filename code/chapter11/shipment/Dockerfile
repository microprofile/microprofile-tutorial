FROM icr.io/appcafe/open-liberty:23.0.0.3-full-java17-openj9-ubi

# Copy config
COPY --chown=1001:0 src/main/liberty/config/ /config/

# Create the app directory
COPY --chown=1001:0 target/shipment.war /config/apps/

ENV VERBOSE=true
ENV MP_CONFIG_PROFILE=docker

RUN configure.sh

EXPOSE 8060 9060

CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
