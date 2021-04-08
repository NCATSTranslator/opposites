#
# When run, this container downloads the Biolink ontology as well as the MetaKG and 
# examines the MetaKG assertions for classes and predicates that are missing from Biolink 
# and for domain/range mismatches. Output is written to multiple files in /home/dev/output
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev

COPY assertions/code /home/dev/code/
COPY assertions/scripts/metakg-assertion-validation.entrypoint.sh /home/dev/
RUN mkdir /home/dev/output
RUN chown -R dev:dev /home/dev

USER dev

# build and install code
WORKDIR /home/dev/code/
RUN mvn clean install

RUN chmod 755 /home/dev/metakg-assertion-validation.entrypoint.sh

ENV MAVEN_OPTS "-Xmx4G"
ENTRYPOINT ["/home/dev/metakg-assertion-validation.entrypoint.sh"] 