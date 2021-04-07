# opposites
This repo serves as a central point of coordination of Translator and external efforts to compile conceptual and relational opposites


## Cataloging current use of RO:0002604 (is_opposite_of) in ontologies
#### System requirements
* [Docker](https://www.docker.com/)

#### Configuration
The list of ontologies that make use of `RO:is_opposite_of` has been manually extracted from [ontobee.org](http://www.ontobee.org/ontology/RO?iri=http://purl.obolibrary.org/obo/RO_0002604). <br> To add an ontology to the analysis, simply add a line to the [download-ontologies-with-is_opposite_of.sh]() script.

#### Execute the analysis of RO:0002604 usage
From the base directory of this repository, execute the analysis with the following commands:
```
docker build -t is-opposite-of-usage -f is_opposite_of-usage.Dockerfile .
docker run --rm -v $PWD/classes/results/is_opposite_of-usage:/home/dev/output is-opposite-of-usage
```

Output is written to [./classes/results/is_opposite_of-usage/is_opposite_of.tsv]().




## Cataloging opposite triples from the MetaKG
#### System requirements
* [Docker](https://www.docker.com/)

#### Configuration
This analysis makes use of a hand-curated list of opposite predicates available in [predicates/predicates.txt](). The MetaKG is automatically downloaded each time the analysis is run.

#### Execute the analysis of opposite triples
From the base directory of this repository, execute the analysis with the following commands:
```
docker build -t metakg-opposite-triples -f metakg-opposite-triples.Dockerfile .
docker run --rm -v $PWD/classes/results/metakg-opposite-triples:/home/dev/output metakg-opposite-triples
```

Output is written to [./classes/results/metakg-opposite-triples/metakg-opposite-triples.tsv]().