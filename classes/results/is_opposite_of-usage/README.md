## is_opposite_of.tsv
The `is_opposite_of.tsv` file in this folder contains pairs of ontology concepts that are observed to be linked using the `RO:is_opposite_of` property. The file is generated/updated when the [is_opposite_of-usage](https://github.com/NCATSTranslator/opposites#cataloging-current-use-of-ro0002604-is_opposite_of-in-ontologies) container is run. File format is tab-delimited:

| label1 | id1 | label2 | id2 | comma-separated list of ontologies where this is_opposite_of assertion was observed |
| ------ | --- | ------ | --- | ----------------------------------------------------------------------------------- |
