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




## Cataloging assertions of oppositeness from the MetaKG
This analysis extracts from the [SmartAPI Translator MetaKG](https://smart-api.info/portal/translator/metakg), assertions of oppositeness, e.g. assertions that share both a subject and object, but use predicates that are opposite in nature. This analysis makes use of a hand-curated list of opposite predicates available in [predicates/predicates.txt](). The [SmartAPI Translator MetaKG](https://smart-api.info/portal/translator/metakg) is automatically downloaded each time the analysis is run.

Output is written to [./classes/results/metakg-assertions-of-oppositeness/metakg-assertions-of-oppositeness.tsv]().

#### System requirements
* [Docker](https://www.docker.com/)

#### Execute the analysis of opposite triples
From the base directory of this repository, execute the analysis with the following commands:
```
docker build -t metakg-assertions-of-oppositeness -f metakg-opposite-triples.Dockerfile .
docker run --rm -v $PWD/classes/results/metakg-assertions-of-oppositeness:/home/dev/output metakg-assertions-of-oppositeness
```

```

Output is written to [./classes/results/metakg-opposite-triples/metakg-opposite-triples.tsv]().