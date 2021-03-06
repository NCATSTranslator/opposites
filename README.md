# opposites
This repo serves as a central point of coordination of Translator and external efforts to compile conceptual and relational opposites

## Relevant external repositories
* [Text Mining Provider's lexical approach to finding opposites](https://github.com/UCDenver-ccp/translator-concept-oppositeness)


## Cataloging current use of RO:0002604 (is_opposite_of) in ontologies
This analysis mines existing ontologies for assertions that make use of the `RO:is_opposite_of` predicate. The list of ontologies that make use of `RO:is_opposite_of` has been manually extracted from [ontobee.org](http://www.ontobee.org/ontology/RO?iri=http://purl.obolibrary.org/obo/RO_0002604). <br> To add an ontology to the analysis, simply add a line to the [download-ontologies-with-is_opposite_of.sh](https://github.com/NCATSTranslator/opposites/blob/main/assertions/scripts/download-ontologies-with-is_opposite_of.sh) script.

Output is written to [./assertions/results/is_opposite_of-usage/is_opposite_of.tsv](https://github.com/NCATSTranslator/opposites/blob/main/assertions/results/is_opposite_of-usage/is_opposite_of.tsv).

#### System requirements
* [Docker](https://www.docker.com/)

#### Execute the analysis of RO:0002604 usage
From the base directory of this repository, execute the analysis with the following commands:
```
docker build -t is-opposite-of-usage -f is_opposite_of-usage.Dockerfile .
docker run --rm -v $PWD/assertions/results/is_opposite_of-usage:/home/dev/output is-opposite-of-usage
```

## Cataloging assertions of oppositeness from the MetaKG
This analysis extracts from the [SmartAPI Translator MetaKG](https://smart-api.info/portal/translator/metakg), assertions of oppositeness, e.g. assertions that share both a subject and object, but use predicates that are opposite in nature. This analysis makes use of a hand-curated list of opposite predicates available in [predicates/predicates.txt](https://github.com/NCATSTranslator/opposites/blob/main/predicates/predicates.txt). The [SmartAPI Translator MetaKG](https://smart-api.info/portal/translator/metakg) is automatically downloaded each time the analysis is run.

Output is written to [./assertions/results/metakg-assertions-of-oppositeness/metakg-assertions-of-oppositeness.tsv](https://github.com/NCATSTranslator/opposites/blob/main/assertions/results/metakg-assertions-of-oppositeness/metakg-assertions-of-oppositeness.tsv).

#### System requirements
* [Docker](https://www.docker.com/)

#### Execute the analysis of opposite triples
From the base directory of this repository, execute the analysis with the following commands:
```
docker build -t metakg-assertions-of-oppositeness -f metakg-assertions-of-oppositeness.Dockerfile .
docker run --rm -v $PWD/assertions/results/metakg-assertions-of-oppositeness:/home/dev/output metakg-assertions-of-oppositeness
```


## Compile lexically-derived opposites into n-triples
This task compiles pairs of lexically-derived ontology concepts into a single n-triples file that links concepts using the `RO:0002604` property. During the processing, pairs of concepts that include at least one deprecated concept are excluded from the output.

Output is written to [./assertions/results/lexically-derived-opposites/lexically-derived-opposites.nt](https://github.com/NCATSTranslator/opposites/blob/main/assertions/results/lexically-derived-opposites/lexically-derived-opposites.nt).

#### System requirements
* [Docker](https://www.docker.com/)

#### To compile the lexically-derived opposites into a single n-triple file
From the base directory of this repository, execute the following commands:
```
docker build -t lexopp -f compile-lexically-derived-opposite-pairs.Dockerfile .
docker run --rm -v $PWD/assertions/results/lexically-derived-opposites:/home/dev/output lexopp
```




## Validating Translator assertions represented in the MetaKG (Deprecated)
**Note: this task is handled by SRI via the Biolink Toolkit. It remains archived here for posterity.**
Note: this task has nothing to do with opposites, but was inspired from observations of domain/range mismatches made while examining the [SmartAPI Translator MetaKG](https://smart-api.info/portal/translator/metakg) for assertions of oppositeness. This analysis examines assertions in the MetaKG and reports on four possible errors:
1) A class used as the subject or object for a given assertion is not present in Biolink
2) The predicate used by an assertion is not present in Biolink
3) The subject of an assertion is not in alignment with the domain of the assertion's predicate
4) The object of an assertion is not in alignment with the range of the assertion's predicate

This analysis makes use of the Biolink ontology and the SmartAPI Translator MetaKG, both of which are downloaded automatically.

Output is written to [./assertions/results/metakg-assertion-validation/metakg-assertion-errors.txt](https://github.com/NCATSTranslator/opposites/blob/main/assertions/results/metakg-assertion-validation/metakg-assertion-errors.txt).

This analysis also outputs a list of Biolink predicates that are missing a domain and/or range to [./assertions/results/metakg-assertion-validation/biolink-predicates-missing-domain-range.tsv](https://github.com/NCATSTranslator/opposites/blob/main/assertions/results/metakg-assertion-validation/biolink-predicates-missing-domain-range.tsv).

#### System requirements
* [Docker](https://www.docker.com/)

#### Execute the analysis of MetaKG assertsion
From the base directory of this repository, execute the analysis with the following commands:
```
docker build -t metakg-assertion-validation -f metakg-assertion-validation.Dockerfile .
docker run --rm -v $PWD/assertions/results/metakg-assertion-validation:/home/dev/output metakg-assertion-validation
```

