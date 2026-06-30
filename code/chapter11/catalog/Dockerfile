FROM icr.io/appcafe/open-liberty:full-java17-openj9-ubi

COPY --chown=1001:0 src/main/liberty/config/ /config/

COPY --chown=1001:0 target/catalog.war /config/apps/

RUN configure.sh

EXPOSE 5050 5051

CMD ["/opt/ol/wlp/bin/server", "run", "defaultServer"]
