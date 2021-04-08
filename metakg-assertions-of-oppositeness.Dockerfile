#
# When run, this container will extract pairs of triples from the MetaKG that share the 
# same subject and object but use predicates that are opposites. Results are output to /home/dev/output.
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev

COPY predicates/predicates.txt /home/dev/data/
COPY assertions/code /home/dev/code/
COPY assertions/scripts/metakg-assertions-of-oppositeness.entrypoint.sh /home/dev/
RUN mkdir /home/dev/output
RUN chown -R dev:dev /home/dev

USER dev

# build and install code
WORKDIR /home/dev/code/
RUN mvn clean install

RUN chmod 755 /home/dev/metakg-assertions-of-oppositeness.entrypoint.sh

ENV MAVEN_OPTS "-Xmx2G"
ENTRYPOINT ["/home/dev/metakg-assertions-of-oppositeness.entrypoint.sh"] 