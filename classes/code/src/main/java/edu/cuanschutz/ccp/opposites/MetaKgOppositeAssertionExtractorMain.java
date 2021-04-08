package edu.cuanschutz.ccp.opposites;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.cuanschutz.ccp.opposites.MetaKgOppositeAssertionExtractor.OppositeTriples;
import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;

/**
 * Extracts triples that share subject & object but contain opposite predicates
 * from the MetaKG
 *
 */
public class MetaKgOppositeAssertionExtractorMain {

	/**
	 * @param args args[0] = a list of opposite predicates (one pair per line,
	 *             comma-delimited) <br>
	 *             args[1] = the directory where the output file
	 *             (metakg-opposite-triples.tsv) will be written
	 * 
	 */
	public static void main(String[] args) {
		File oppositePredicatesFile = new File(args[0]);
		File outputDirectory = new File(args[1]);
		File outputFile = new File(outputDirectory, "metakg-assertions-of-oppositeness.tsv");

		try {
			Map<String, String> oppositePredicatesMap = buildOppositePredicatesMap(
					new FileInputStream(oppositePredicatesFile));
			Collection<OppositeTriples> oppositeTriples = MetaKgOppositeAssertionExtractor
					.extractOppositeMetaKgTriples(oppositePredicatesMap);

			List<String> lines = createOutputLines(oppositeTriples);

			try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputFile)) {
				for (String line : lines) {
					writer.write(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * Serializes the input collection of opposite triples into Strings
	 * 
	 * @param oppositeTriples
	 * @return
	 */
	protected static List<String> createOutputLines(Collection<OppositeTriples> oppositeTriples) {
		List<String> lines = new ArrayList<String>();
		for (OppositeTriples ot : oppositeTriples) {
			Triple t1 = ot.getT1();
			Triple t2 = ot.getT2();

			/* subject and object need to be the same at this point */
			if (t1.getSubject().equals(t2.getSubject()) && t1.getObject().equals(t2.getObject())) {
				String subject = t1.getSubject();
				String object = t1.getObject();

				String predicate1 = t1.getPredicate();
				String predicate2 = t2.getPredicate();

				StringBuilder sb = new StringBuilder();

				sb.append(subject + "\t" + object);

				if (predicate1.compareTo(predicate2) < 1) {
					sb.append("\t" + predicate1 + "\t" + predicate2 + "\t" + getTeamApiString(t1) + "\t"
							+ getTeamApiString(t2) + "\n");
				} else {
					sb.append("\t" + predicate2 + "\t" + predicate1 + "\t" + getTeamApiString(t2) + "\t"
							+ getTeamApiString(t1) + "\n");
				}

				lines.add(sb.toString());

			} else {
				throw new IllegalStateException("triple s-p-o mismatch");
			}

		}

		Collections.sort(lines);
		return lines;
	}

	/**
	 * @param t
	 * @return a comma-delimited list of TEAM_NAME|API_NAME pairs
	 */
	public static String getTeamApiString(Triple t) {
		List<String> teamApiNames = new ArrayList<String>();
		for (Entry<String, String> entry : t.getTeamToApiMap().entrySet()) {
			teamApiNames.add(entry.getKey() + "|" + entry.getValue());
		}
		Collections.sort(teamApiNames);

		return CollectionsUtil.createDelimitedString(teamApiNames, ",");
	}

	/**
	 * Parse a file where each line contains two opposite predicates separated by a
	 * comma
	 * 
	 * @param fileInputStream
	 * @return
	 * @throws IOException
	 */
	private static Map<String, String> buildOppositePredicatesMap(FileInputStream fileInputStream) throws IOException {
		Map<String, String> oppositePredicateMap = new HashMap<String, String>();
		for (StreamLineIterator lineIter = new StreamLineIterator(fileInputStream, CharacterEncoding.UTF_8,
				null); lineIter.hasNext();) {
			String line = lineIter.next().getText();
			String[] cols = line.split(",");

			if (cols.length == 2) {
				oppositePredicateMap.put(cols[0].trim(), cols[1].trim());
			} else {
				throw new IllegalArgumentException(String
						.format("Expected two columns but found %d. Unable to parse line: %s", cols.length, line));
			}
		}
		return oppositePredicateMap;
	}
}
