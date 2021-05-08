#!/bin/bash

mvn exec:java -Dexec.mainClass='edu.cuanschutz.ccp.opposites.OppositePairToRdfMain' -Dexec.args='/home/dev/data /home/dev/ontologies /home/dev/output'