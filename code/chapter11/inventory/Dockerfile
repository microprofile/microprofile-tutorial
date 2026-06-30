FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi

COPY --chown=1001:0 src/main/liberty/config/ /config/

COPY --chown=1001:0 target/inventory.war /config/apps/

RUN configure.sh

EXPOSE 7050 7051

CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
