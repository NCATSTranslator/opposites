#
# When run, this container will extract pairs of classes linked with the 
# RO:is_opposite_of property and output them to a file in /home/dev/output
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev

COPY classes/code /home/dev/code/
COPY classes/scripts/download-ontologies-with-is_opposite_of.sh /home/dev/scripts/
COPY classes/scripts/is_opposite_of-usage.entrypoint.sh /home/dev/
RUN mkdir -p /home/dev/data/ontologies && \
    mkdir /home/dev/output
RUN chown -R dev:dev /home/dev

USER dev

# download the ontologies that make use of RO:is_opposite_of
WORKDIR /home/dev/data/ontologies
RUN chmod 755 /home/dev/scripts/download-ontologies-with-is_opposite_of.sh && \
    sh /home/dev/scripts/download-ontologies-with-is_opposite_of.sh

# build and install code
WORKDIR /home/dev/code/
RUN mvn clean install

RUN chmod 755 /home/dev/is_opposite_of-usage.entrypoint.sh

ENV MAVEN_OPTS "-Xmx4G"
ENTRYPOINT ["/home/dev/is_opposite_of-usage.entrypoint.sh"] 



