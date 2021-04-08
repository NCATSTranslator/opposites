#!/bin/bash

mvn exec:java -Dexec.mainClass='edu.cuanschutz.ccp.opposites.MetaKgOppositeAssertionExtractorMain' -Dexec.args='/home/dev/data/predicates.txt /home/dev/output'