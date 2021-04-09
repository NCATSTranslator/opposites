## biolink-predicates-missing-domain-range.tsv
The `biolink-predicates-missing-domain-range.tsv` file catalogs predicates in the Biolink ontology that are missing domain and/or range assignments. Domain/range information is used to validate the MetaKG using the Biolink ontology, so it can be helpful to know if there are predicates missing domain and/or range assignments. This file is generated/updated when the [metakg-assertion-validation](https://github.com/NCATSTranslator/opposites#validating-translator-assertions-represented-in-the-metakg) container is run.

## metakg-assertion-errors.txt
The `metakg-assertion-errors.txt` file logs errors in MetaKG assertions based on validation against the Biolink ontology. This file is generated/updated when the [metakg-assertion-validation](https://github.com/NCATSTranslator/opposites#validating-translator-assertions-represented-in-the-metakg) container is run. Four types of errors are detected:

  1.  A class used as the subject or object for a given assertion is not present in Biolink
  1.  The predicate used by an assertion is not present in Biolink
  1.  The subject of an assertion is not subsumed by the domain of the assertion's predicate
  1.  The object of an assertion is not subsumed by the range of the assertion's predicate

An explanation for each error is output to a separate line in the file. Note that for cases when the subject or object in an assertion is a `mixin`, each descendant of the mixin is validated. When an error is detected in these cases, you will find the mixin class listed along with the erroring class for completeness, e.g. if a domain mismatch is detected involving `biolinkActivity` where the subject in the assertion is `biolink:Occurrent` (a mixin, and the parent of `biolink:Activity`), then the error message will describe the offending class as `https://w3id.org/biolink/vocab/Occurrent|https://w3id.org/biolink/vocab/Activity`. <br>
File format is tab-delimited: 
| TEAM_NAME\|API_NAME | ERROR_MESSAGE |
| ------------------- | ------------- |

#### Example error message

> TEAM_NAME|API_NAME  Invalid domain detected. Subject: https://w3id.org/biolink/vocab/EnvironmentalProcess is not a [https://w3id.org/biolink/vocab/BiologicalProcessOrActivity]. In 'EnvironmentalProcess ----> enabled_by ----> MolecularEntity'

**Why has this example assertion been flagged?**

The MetaKG assertion in question is `EnvironmentalProcess ----> enabled_by ----> MolecularEntity`.
In the Biolink ontology `biolink:enabled_by` is defined to have a domain of `biolink:BiologicalProcessOrActivity`.
The subject of this assertion, `biolink:EnvironmentalProcess` exists in the Biolink ontology; however it is a sibling of `biolink:BiologicalProcessOrActivity` rather than a descendant, and therefore there is a mismatch between the specified domain of the predicate and the subject of the assertion.

**What is the solution to addressing this problem?**

There are two possible solutions to address this issue:
1. Replace the use of `enabled_by` with a predicate that has an appropriate domain and range for the given assertion.
2. Propose an update to the Biolink ontology to revise the domain of `enabled_by` in order to bring this assertion into compliance.
