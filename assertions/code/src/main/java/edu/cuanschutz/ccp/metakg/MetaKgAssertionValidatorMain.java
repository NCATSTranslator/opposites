package edu.cuanschutz.ccp.metakg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.cuanschutz.ccp.biolink.BiolinkDomainRangeValidator;
import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.ucdenver.ccp.common.file.FileWriterUtil;

/**
 * Automatically downloads the Biolink ontology and the MetaKG JSON and
 * validates the assertions specified in the MetaKG.
 * 
 * This analysis examines assertions in the MetaKG and reports on four possible
 * errors:
 * <ol>
 * <li>A class used as the subject or object for a given assertion is not
 * present in Biolink
 * <li>The predicate used by an assertion is not present in Biolink
 * <li>The subject of an assertion is not in alignment with the domain of the
 * assertion's predicate
 * <li>The object of an assertion is not in alignment with the range of the
 * assertion's predicate
 * </ol>
 *
 */
public class MetaKgAssertionValidatorMain {

	private static final Logger logger = Logger.getLogger(MetaKgAssertionValidatorMain.class);

	private static final String BIOLINK_OWL_URL = "https://github.com/biolink/biolink-model/raw/master/biolink-model.owl.ttl";

	public static void main(String[] args) {
		File outputDirectory = new File(args[0]);
		File validationErrorFile = new File(outputDirectory, "metakg-assertion-errors.txt");
		File missingDomainRangeReportFile = new File(outputDirectory, "biolink-predicates-missing-domain-range.tsv");

		/* Open the Biolink OWL stream */
		InputStream biolinkStream = null;
		try {
			URL biolinkUrl = new URL(BIOLINK_OWL_URL);
			biolinkStream = biolinkUrl.openStream();
		} catch (IOException e) {
			logger.error(String.format(
					"The Biolink OWL file (biolink-model.owl.ttl) appears to be unavailable. Analysis cannot continue.\n%s %s\nExiting...",
					e.getClass().getName(), e.getMessage()));
			System.exit(-1);
		}

		/* Open the MetaKG JSON stream */
		InputStreamReader isr = null;
		try {
			URL metaKgUrl = new URL(MetaKgParser.META_KG_URL);
			isr = new InputStreamReader(metaKgUrl.openStream());
		} catch (IOException e) {
			logger.error(String.format("MetaKG appears to be unavailable. Analysis cannot continue.\n%s %s\nExiting...",
					e.getClass().getName(), e.getMessage()));
			System.exit(-1);
		}

		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(validationErrorFile)) {
			Map<String, Set<Triple>> tripleMap = MetaKgParser.parseMetaKgJson(isr);

			BiolinkDomainRangeValidator pv = new BiolinkDomainRangeValidator(biolinkStream,
					missingDomainRangeReportFile);
			for (Entry<String, Set<Triple>> entry : tripleMap.entrySet()) {
				for (Triple t : entry.getValue()) {
					pv.validate(t, writer);
				}
			}
		} catch (IOException | OWLOntologyCreationException e) {
			logger.error("Analysis failed during MetaKG processing. Exiting...", e);
			System.exit(-1);
		}
	}
}
