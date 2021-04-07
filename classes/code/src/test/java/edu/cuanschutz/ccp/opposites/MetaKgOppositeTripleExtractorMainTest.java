package edu.cuanschutz.ccp.opposites;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.cuanschutz.ccp.opposites.MetaKgOppositeTripleExtractor.OppositeTriples;

public class MetaKgOppositeTripleExtractorMainTest {

	@Test
	public void testCreateOutputLines() {

		Triple expectedTriple1p = new Triple("s1", "p1", "o1");
		expectedTriple1p.addTeamApi("t1", "a1");

		Triple expectedTriple1q = new Triple("s1", "q1", "o1");
		expectedTriple1q.addTeamApi("t2", "a2");

		Triple expectedTriple2p = new Triple("s2", "p2", "o2");
		expectedTriple2p.addTeamApi("t4", "a4");
		expectedTriple2p.addTeamApi("t5", "a5");

		Triple expectedTriple2q = new Triple("s2", "q2", "o2");
		expectedTriple2q.addTeamApi("t6", "a6");

		Set<OppositeTriples> oppositeTriples = new HashSet<OppositeTriples>();
		oppositeTriples.add(new OppositeTriples(expectedTriple1p, expectedTriple1q));
		oppositeTriples.add(new OppositeTriples(expectedTriple2p, expectedTriple2q));

		List<String> outputLines = MetaKgOppositeTripleExtractorMain.createOutputLines(oppositeTriples);

		List<String> expectedOutputLines = Arrays.asList("s1\to1\tp1\tq1\tt1|a1\tt2|a2\n",
				"s2\to2\tp2\tq2\tt4|a4,t5|a5\tt6|a6\n");

		assertEquals(expectedOutputLines, outputLines);

	}

}
