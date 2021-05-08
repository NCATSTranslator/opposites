package edu.cuanschutz.ccp.opposites;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OppositePairToRdfMain {

	/**
	 * @param args args[0] = oppositesDirectory = directory containing manually
	 *             vetted opposite pairs files (5 columns, X - label1 - id1 - label2
	 *             - id2) <br>
	 *             args[1] = ontologyDirectory = directory containing ontology files
	 *             that will be used to exclude obsolete classes from the output
	 *             <br>
	 *             args[2] = outputDirectory = directory where n-triples output will
	 *             be written; file will be named lexically-derived-opposites.nt
	 *             <br>
	 */
	public static void main(String[] args) {

		File oppositesDirectory = new File(args[0]);
		File ontologyDirectory = new File(args[1]);
		File outputDirectory = new File(args[2]);
//		
		
//		File oppositesDirectory = new File("/Users/bill/projects/ncats-translator/opposites/opposites.git/tmp");
//		File ontologyDirectory = new File("/tmp/ontologies");
//		File outputDirectory = new File("/tmp/rdf");
		
		

		try {
			OppositePairToRdf.processOppositePairs(oppositesDirectory, ontologyDirectory, outputDirectory);
		} catch (OWLOntologyCreationException | IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
