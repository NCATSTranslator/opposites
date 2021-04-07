package edu.cuanschutz.ccp.opposites;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.cuanschutz.ccp.opposites.IsOppositeOfExtractor.OntConcept;
import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.collections.CollectionsUtil.SortOrder;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;

/**
 * Extracts subject-object pairs from a set of ontology files where the subject
 * and object are linked via RO:0002604 (is_opposite_of)
 *
 */
public class IsOppositeOfExtractorMain {

	/**
	 * @param args args[0] = directory containing ontologies as .owl files <br>
	 *             args[1] = output directory where is_opposite_of.tsv file will be
	 *             written
	 * 
	 */
	public static void main(String[] args) {

		File ontDir = new File(args[0]);
		File outputDir = new File(args[1]);
		File outputFile = new File(outputDir, "is_opposite_of.tsv");

		/*
		 * the following map is use to track the files that contain particular opposite
		 * relations. The key String is "IRI1 [tab] IRI2" while the value is a set of
		 * filenames where that opposite_of relation has been observed.
		 */
		Map<String, Set<String>> oppositesToFileMap = new HashMap<String, Set<String>>();
		Map<String, String> conceptIriToLabelMap = new HashMap<String, String>();

		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			for (Iterator<File> fileIterator = FileUtil.getFileIterator(ontDir, false, ".owl"); fileIterator
					.hasNext();) {
				File ontFile = fileIterator.next();
				System.out.println("Processing: " + ontFile.getName());
				InputStream ontologyStream = new FileInputStream(ontFile);
				Map<OntConcept, OntConcept> oppositeClasses = IsOppositeOfExtractor
						.extractOppositeClasses(ontologyStream);

				/*
				 * save labels -- some ontologies include labels for the concepts, e.g. PATO
				 * classes, and some do not so we catalog them here to be output later
				 */

				cacheLabels(conceptIriToLabelMap, oppositeClasses.keySet());
				cacheLabels(conceptIriToLabelMap, oppositeClasses.values());

				for (Entry<OntConcept, OntConcept> entry : oppositeClasses.entrySet()) {
					String key = entry.getKey().getIri() + "\t" + entry.getValue().getIri();
					CollectionsUtil.addToOne2ManyUniqueMap(key, ontFile.getName(), oppositesToFileMap);
				}
			}

			Map<String, Set<String>> sortedMap = CollectionsUtil.sortMapByKeys(oppositesToFileMap, SortOrder.ASCENDING);
			for (Entry<String, Set<String>> entry : sortedMap.entrySet()) {
				String[] cols = entry.getKey().split("\\t");
				String iri1 = cols[0];
				String iri2 = cols[1];

				String label1 = conceptIriToLabelMap.get(iri1);
				String label2 = conceptIriToLabelMap.get(iri2);

				if (label1 == null) {
					label1 = "LABEL_NOT_AVAILABLE";
				}

				if (label2 == null) {
					label2 = "LABEL_NOT_AVAILABLE";
				}

				String fileNames = CollectionsUtil.createDelimitedString(entry.getValue(), ",");

				List<String> sortedLabels = Arrays.asList(label1, label2);
				Collections.sort(sortedLabels);

				// sort by labels for kev
				if (sortedLabels.get(0).equals(label1)) {
					writer.write(String.format("%s\t%s\t%s\t%s\t%s\n", label1, iri1, label2, iri2, fileNames));
				} else {
					writer.write(String.format("%s\t%s\t%s\t%s\t%s\n", label2, iri2, label1, iri1, fileNames));
				}
			}
		} catch (OWLOntologyCreationException | IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private static void cacheLabels(Map<String, String> conceptIriToLabelMap, Collection<OntConcept> oppositeClasses) {
		for (OntConcept oc : oppositeClasses) {
			if (oc.getLabel() != null) {
				conceptIriToLabelMap.put(oc.getIri(), oc.getLabel());
			}
		}
	}

}
