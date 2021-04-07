#!/bin/bash

mvn exec:java -Dexec.mainClass='edu.cuanschutz.ccp.opposites.MetaKgOppositeTripleExtractorMain' -Dexec.args='/home/dev/data/predicates.txt /home/dev/output'