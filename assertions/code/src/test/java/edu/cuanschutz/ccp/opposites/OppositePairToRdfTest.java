package edu.cuanschutz.ccp.opposites;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.rio.ntriples.NTriplesWriter;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileComparisonUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.FileComparisonUtil.ColumnOrder;
import edu.ucdenver.ccp.common.file.FileComparisonUtil.LineOrder;
import edu.ucdenver.ccp.common.file.FileReaderUtil;

public class OppositePairToRdfTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testProcessOppositePairFile() throws IOException {
		File outputFile = folder.newFile();

		String oppositeFileContent = "X\tprimary motor neuron\tCL:0000533\tsecondary motor neuron\tCL:0000536\n"
				+ "\tasexual spore\tCL:0000605\tsexual spore\tCL:0000596\n"
				+ "X\tprimary pigment cell\tCL:0000727\tsecondary pigment cell\tCL:0000728\n"
				+ "OBSOLETE?\tobsolete backcross fertile\tPATO:0000900\tobsolete backcross sterile\tPATO:0000901\n"
				+ "\topaque\tPATO:0000963\ttransparent\tPATO:0000964\n";

		Set<String> obsoleteIds = CollectionsUtil.createSet("http://purl.obolibrary.org/obo/PATO_0000900");

		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
			NTriplesWriter rdfWriter = new NTriplesWriter(writer);
			rdfWriter.startRDF();
			OppositePairToRdf.processOppositePairFile(new ByteArrayInputStream(oppositeFileContent.getBytes()),
					obsoleteIds, rdfWriter);
			rdfWriter.endRDF();
		}

		List<String> expectedLines = Arrays.asList(
				"<http://purl.obolibrary.org/obo/CL_0000605> <" + OppositePairToRdf.RO_IS_OPPOSITE_OF
						+ "> <http://purl.obolibrary.org/obo/CL_0000596> .",
				"<http://purl.obolibrary.org/obo/PATO_0000963> <" + OppositePairToRdf.RO_IS_OPPOSITE_OF
						+ "> <http://purl.obolibrary.org/obo/PATO_0000964> .");

		for (String s : FileReaderUtil.loadLinesFromFile(outputFile, CharacterEncoding.UTF_8)) {
			System.out.println("LINE: " + s);
		}

		assertTrue(FileComparisonUtil.hasExpectedLines(outputFile, CharacterEncoding.UTF_8, expectedLines, null,
				LineOrder.AS_IN_FILE, ColumnOrder.AS_IN_FILE));

	}

}
