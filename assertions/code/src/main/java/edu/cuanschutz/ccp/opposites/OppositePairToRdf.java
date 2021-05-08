package edu.cuanschutz.ccp.opposites;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openrdf.model.IRI;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.ntriples.NTriplesWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import owltools.graph.OWLGraphWrapper;

public class OppositePairToRdf {

	private static final Logger logger = Logger.getLogger(OppositePairToRdf.class);

	public static final String OBO_PURL = "http://purl.obolibrary.org/obo/";
	public static final String RO_IS_OPPOSITE_OF = OBO_PURL + "RO_0002604";

	/**
	 * @param oppositesDirectory directory containing manually vetted opposite pairs
	 *                           files (5 columns, X - label1 - id1 - label2 - id2)
	 * @param ontologyDirectory  directory containing ontology files that will be
	 *                           used to exclude obsolete classes from the output
	 * @param outputDirectory    directory where n-triples output will be written;
	 *                           file will be named lexically-derived-opposites.nt
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 */
	public static void processOppositePairs(File oppositesDirectory, File ontologyDirectory, File outputDirectory)
			throws OWLOntologyCreationException, IOException {

		Set<String> obsoleteIds = loadObsoleteClassIds(ontologyDirectory);
		File outputFile = new File(outputDirectory, "lexically-derived-opposites.nt");
		processOppositePairDirectory(oppositesDirectory, obsoleteIds, outputFile);

	}

	/**
	 * load ids for obsolete classes from all files in the input directory; files
	 * are assumed to be ontology files
	 * 
	 * @param ontologyDirectory
	 * @return
	 * @throws IOException
	 * @throws OWLOntologyCreationException
	 */
	private static Set<String> loadObsoleteClassIds(File ontologyDirectory)
			throws IOException, OWLOntologyCreationException {
		Set<String> obsoleteIds = new HashSet<String>();
		for (Iterator<File> fileIterator = FileUtil.getFileIterator(ontologyDirectory, false); fileIterator
				.hasNext();) {
			File ontFile = fileIterator.next();
			logger.info("Loading obsolete classes from: " + ontFile.getAbsolutePath());
			obsoleteIds.addAll(loadObsoleteClassIds(new FileInputStream(ontFile)));
			logger.info("Obsolete class count: " + obsoleteIds.size());
		}
		return obsoleteIds;
	}

	/**
	 * @param ontStream
	 * @return a set of identifiers for obsolete ontology classes that are part of
	 *         the ontology input stream
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 */
	private static Set<String> loadObsoleteClassIds(InputStream ontStream)
			throws OWLOntologyCreationException, IOException {
		Set<String> obsoleteIds = new HashSet<String>();

		OWLOntologyManager inputOntologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = inputOntologyManager.loadOntologyFromOntologyDocument(ontStream);
		try (OWLGraphWrapper graph = new OWLGraphWrapper(ont)) {
			for (OWLObject owlObject : graph.getAllOWLObjects()) {
				if (owlObject instanceof OWLClass) {
					OWLClass cls = (OWLClass) owlObject;
					if (isObsolete(cls, ont)) {
						obsoleteIds.add(cls.getIRI().toString());
					}
				}
			}
		}

		return obsoleteIds;
	}

	private static boolean isObsolete(OWLClass cls, OWLOntology ont) {
		Set<OWLAnnotation> annotations = cls.getAnnotations(ont);
		for (OWLAnnotation annotation : annotations) {
			if (annotation.isDeprecatedIRIAnnotation()) {
				return true;
			}
		}
		return false;
	}

	public static void processOppositePairDirectory(File directory, Set<String> obsoleteIds, File outputFile)
			throws IOException {

		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			NTriplesWriter rdfWriter = new NTriplesWriter(writer);
			rdfWriter.startRDF();

			for (Iterator<File> fileIterator = FileUtil.getFileIterator(directory, false); fileIterator.hasNext();) {
				File oppositesFile = fileIterator.next();
				logger.info("Processing opposites file: " + oppositesFile.getAbsolutePath());
				processOppositePairFile(new FileInputStream(oppositesFile), obsoleteIds, rdfWriter);
			}

			rdfWriter.endRDF();
		}

	}

	public static void processOppositePairFile(InputStream pairStream, Set<String> obsoleteIds, NTriplesWriter writer)
			throws IOException {
		ValueFactory factory = SimpleValueFactory.getInstance();
		for (StreamLineIterator lineIter = new StreamLineIterator(pairStream, CharacterEncoding.UTF_8, null); lineIter
				.hasNext();) {

			String line = lineIter.next().getText();
			String[] cols = line.split("\\t");

			boolean exclude = cols[0].equals("X");

			if (!exclude) {
				if (cols.length != 5) {
					throw new IllegalArgumentException(
							"Expected 5 columns on line, but observed: " + cols.length + " -- " + line);
				}

				/* include this pair unless one of the concepts is deprecated/obsolete */
				String id1 = addOboPrefix(cols[2]);
				String id2 = addOboPrefix(cols[4]);

				if (!obsoleteIds.contains(id1) && !obsoleteIds.contains(id2)) {
					IRI uri1 = factory.createIRI(id1);
					IRI uri2 = factory.createIRI(id2);
					IRI pred = factory.createIRI(RO_IS_OPPOSITE_OF);
					Statement stmt = factory.createStatement(uri1, pred, uri2);
					writer.handleStatement(stmt);
				} else {
					System.out.println("Excluding due to obsolete class: " + line);
				}

			}

		}
	}

	private static String addOboPrefix(String id) {
		return OBO_PURL + id.replace(":", "_");
	}

}
