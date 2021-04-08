package edu.cuanschutz.ccp.biolink;

import static edu.cuanschutz.ccp.biolink.BiolinkDomainRangeValidator.BIOLINK_NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileReaderUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;

public class BiolinkDomainRangeValidatorTest {

	private static final String BIOLINK_OWL_FILE_NAME = "biolink-model.owl.ttl.gz";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testIsMixin() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		assertFalse(validator.isMixin(String.format("%s%s", BIOLINK_NS, "Activity")));
		assertTrue(validator.isMixin(String.format("%s%s", BIOLINK_NS, "ActivityAndBehavior")));
		assertTrue(validator.isMixin(String.format("%s%s", BIOLINK_NS, "Occurrent")));

	}

	@Test
	public void testExpandMixins() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		Set<String> expanded = new HashSet<String>(
				validator.expandMixins(String.format("%s%s", BIOLINK_NS, "GeneOrGeneProduct")));

		Set<String> expectedExpanded = new HashSet<String>(Arrays.asList(String.format("%s%s", BIOLINK_NS, "Gene"),
				String.format("%s%s", BIOLINK_NS, "ProteinIsoform"),
				String.format("%s%s", BIOLINK_NS, "NoncodingRNAProduct"),
				String.format("%s%s", BIOLINK_NS, "RNAProductIsoform"), String.format("%s%s", BIOLINK_NS, "MicroRNA"),
				String.format("%s%s", BIOLINK_NS, "Protein"), String.format("%s%s", BIOLINK_NS, "SiRNA"),
				String.format("%s%s", BIOLINK_NS, "RNAProduct")));

		assertEquals(expectedExpanded.size(), expanded.size());
		assertEquals(expectedExpanded, expanded);

	}

	@Test
	public void testValidateDomainClassNotInBiolink() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "THIS_CLASS_NOT_IN_BIOLINK";
			String p = "process_negatively_regulates_process";
			String o = "BiologicalProcess";

			String relation = "s --> p --> o";
			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			String predicate = String.format("%s%s", BIOLINK_NS, p);
			String subject = String.format("%s%s", BIOLINK_NS, s);
			validator.validateDomain(t, writer, subject, predicate, relation);
		}
		String line = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).get(0);

		assertTrue(line.contains("Invalid Biolink class observed."));

	}

	@Test
	public void testValidateDomainValidSubject() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "Activity";
			String p = "process_negatively_regulates_process";
			String o = "BiologicalProcess";

			String relation = "s --> p --> o";
			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			String predicate = String.format("%s%s", BIOLINK_NS, p);
			String subject = String.format("%s%s", BIOLINK_NS, s);
			validator.validateDomain(t, writer, subject, predicate, relation);
		}
		int lineCount = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).size();

		assertEquals("no errors, so there should be no lines in the log file", 0, lineCount);

	}

	@Test
	public void testValidateDomainInvalidSubject() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "BiologicalEntity";
			String p = "process_negatively_regulates_process";
			String o = "BiologicalProcess";

			String relation = "s --> p --> o";
			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			String predicate = String.format("%s%s", BIOLINK_NS, p);
			String subject = String.format("%s%s", BIOLINK_NS, s);
			validator.validateDomain(t, writer, subject, predicate, relation);
		}
		String line = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).get(0);

		assertTrue(line.contains("Invalid domain detected"));

	}

	@Test
	public void testValidateRangeClassNotInBiolink() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "Activity";
			String p = "process_negatively_regulates_process";
			String o = "THIS_CLASS_NOT_IN_BIOLINK";

			String relation = "s --> p --> o";
			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			String predicate = String.format("%s%s", BIOLINK_NS, p);
			String object = String.format("%s%s", BIOLINK_NS, o);
			validator.validateRange(t, writer, predicate, object, relation);
		}
		String line = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).get(0);

		assertTrue(line.contains("Invalid Biolink class observed."));

	}

	@Test
	public void testValidateRangeValidSubject() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "Activity";
			String p = "process_negatively_regulates_process";
			String o = "BiologicalProcess";

			String relation = "s --> p --> o";
			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			String predicate = String.format("%s%s", BIOLINK_NS, p);
			String object = String.format("%s%s", BIOLINK_NS, o);
			validator.validateRange(t, writer, predicate, object, relation);
		}
		int lineCount = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).size();

		assertEquals("no errors, so there should be no lines in the log file", 0, lineCount);

	}

	@Test
	public void testValidateRangeInvalidSubject() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "BiologicalProcess";
			String p = "process_negatively_regulates_process";
			String o = "BiologicalEntity";

			String relation = "s --> p --> o";
			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			String predicate = String.format("%s%s", BIOLINK_NS, p);
			String object = String.format("%s%s", BIOLINK_NS, o);
			validator.validateRange(t, writer, predicate, object, relation);
		}
		String line = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).get(0);

		assertTrue(line.contains("Invalid range detected"));

	}

	@Test
	public void testValidateInvalidPredicate() throws OWLOntologyCreationException, IOException {
		File domainRangeReportFile = folder.newFile();
		InputStream biolinkStream = new GZIPInputStream(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), BIOLINK_OWL_FILE_NAME));
		BiolinkDomainRangeValidator validator = new BiolinkDomainRangeValidator(biolinkStream, domainRangeReportFile);

		File outputFile = folder.newFile();
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			String s = "BiologicalProcess";
			String p = "this_predicate_not_in_biolink";
			String o = "BiologicalEntity";

			Triple t = new Triple(s, p, o);
			t.addTeamApi("team", "api");
			validator.validate(t, writer);
		}
		String line = FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8, null).get(0);

		assertTrue(line.contains("Invalid Biolink predicate observed."));

	}

}
