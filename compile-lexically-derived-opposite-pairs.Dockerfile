#
# When run, this container will compile pairs of opposites that have been 
# derived from lexical analysis of the concept labels. The opposite pairs
# are aggregated and output as an n-triples file to /home/dev/output.
#
FROM adoptopenjdk:8-jdk

RUN apt-get update && apt-get install -y \
    maven \
    wget

RUN groupadd --gid 9001 dev && \
    useradd --create-home --shell /bin/bash --no-log-init -u 9001 -g dev dev

# download ontologies - they are used to exclude obsolete classes
WORKDIR /home/dev/ontologies
RUN wget http://purl.obolibrary.org/obo/cl.owl && \
    wget http://purl.obolibrary.org/obo/chebi.owl && \
    wget http://purl.obolibrary.org/obo/go.owl && \
    wget http://purl.obolibrary.org/obo/hp.owl && \
    wget http://purl.obolibrary.org/obo/mp.owl && \
    # excluding ncbitaxon simply to speed up processing. There aren't many 
    # opposite taxon pairs so they can be manually vetted for obsoleteness.
    #wget http://purl.obolibrary.org/obo/ncbitaxon.owl && \
    wget http://purl.obolibrary.org/obo/pato.owl && \
    wget http://purl.obolibrary.org/obo/pr.owl && \
    wget http://purl.obolibrary.org/obo/so.owl

WORKDIR /home/dev/data
RUN wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/cell_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/chebi_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/go_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/hp_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/mpo_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/ncbitaxon_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/pato_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/pro_opposites.tsv && \
    wget https://raw.githubusercontent.com/UCDenver-ccp/translator-concept-oppositeness/main/experimental-outputs/currentOpposites_manually-vetted/so_opposites.tsv

COPY assertions/code /home/dev/code/
COPY assertions/scripts/compile-lexically-derived-opposites.entrypoint.sh /home/dev/
RUN mkdir /home/dev/output
RUN chown -R dev:dev /home/dev

USER dev

# build and install code
WORKDIR /home/dev/code/
RUN mvn clean install

RUN chmod 755 /home/dev/*.sh

ENV MAVEN_OPTS "-Xmx6G"
ENTRYPOINT ["/home/dev/compile-lexically-derived-opposites.entrypoint.sh"] 