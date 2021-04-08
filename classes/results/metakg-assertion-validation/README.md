## biolink-predicates-missing-domain-range.tsv
The `biolink-predicates-missing-domain-range.tsv` file catalogs predicates in the Biolink ontology that are missing domain and/or range assignments.

## metakg-assertion-errors.txt
The `metakg-assertion-errors.txt` file logs errors in MetaKG assertions based on validation against the Biolink ontology. Four types of errors are detected:

  1.  A class used as the subject or object for a given assertion is not present in Biolink
  1.  The predicate used by an assertion is not present in Biolink
  1.  The subject of an assertion is not in alignment with the domain of the assertion's predicate
  1.  The object of an assertion is not in alignment with the range of the assertion's predicate

An explanation for each error is output to a separate line in the file. Note that for cases when the subject or object in an assertion is a `mixin`, each descendent of the mixin is validated. When an error is detected in these cases, the mixin class is listed along with the erroring class for completeness, e.g. `https://w3id.org/biolink/vocab/Occurrent|https://w3id.org/biolink/vocab/Activity` where `biolink:Occurrent` is a mixin and `biolink:Activity` is a subclass of `biolink:Occurrent`. <br>
File format: `TEAM_NAME|API_NAME` [tab] `ERROR_MESSAGE`

#### Example error message

> TEAM_NAME|API_NAME  Invalid domain detected. Subject: https://w3id.org/biolink/vocab/EnvironmentalProcess is not a [https://w3id.org/biolink/vocab/BiologicalProcessOrActivity]. In 'EnvironmentalProcess ----> enabled_by ----> MolecularEntity'

#### Explanation
**Why has this example assertion been flagged?**
The MetaKG assertion in question is `EnvironmentalProcess ----> enabled_by ----> MolecularEntity`.
In the Biolink ontology `biolink:enabled_by` is defined to have a domain of `biolink:BiologicalProcessOrActivity`.
The subject of this assertion, `biolink:EnvironmentalProcess` exists in the Biolink ontology, however it is not a descendent of `biolink:BiologicalProcessOrActivity` (it is a sibling concept instead), and therefore there is a mismatch between the expected domain of the predicate and the subject of the assertion.

