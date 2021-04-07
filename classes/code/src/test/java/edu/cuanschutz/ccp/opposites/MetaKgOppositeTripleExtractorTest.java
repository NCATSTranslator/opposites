package edu.cuanschutz.ccp.opposites;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.cuanschutz.ccp.opposites.MetaKgOppositeTripleExtractor.OppositeTriples;

public class MetaKgOppositeTripleExtractorTest {

	@Test
	public void testCombineTriples() {
		Triple t1 = new Triple("s", "p", "o");
		t1.addTeamApi("t1", "a1");
		Triple t2 = new Triple("s", "p", "o");
		t2.addTeamApi("t2", "a2");
		Triple t3 = new Triple("s", "p", "o");
		t3.addTeamApi("t3", "a3");
		Triple t4 = new Triple("s", "p", "o");
		t4.addTeamApi("t4", "a4");
		t4.addTeamApi("t5", "a5");

		Triple combinedTriple = MetaKgOppositeTripleExtractor
				.combineTriples(new HashSet<Triple>(Arrays.asList(t1, t2, t3, t4)));

		Triple expectedTriple = new Triple("s", "p", "o");
		expectedTriple.addTeamApi("t1", "a1");
		expectedTriple.addTeamApi("t2", "a2");
		expectedTriple.addTeamApi("t3", "a3");
		expectedTriple.addTeamApi("t4", "a4");
		expectedTriple.addTeamApi("t5", "a5");

		assertEquals(expectedTriple, combinedTriple);

	}

	@Test
	public void testOppositeTriplesHashCode() {
		Triple expectedTriple1p = new Triple("s1", "p", "o1");
		expectedTriple1p.addTeamApi("t1", "a1");

		Triple expectedTriple1q = new Triple("s1", "q", "o1");
		expectedTriple1p.addTeamApi("t2", "a2");

		Triple expectedTriple2p = new Triple("s2", "p", "o2");
		expectedTriple1p.addTeamApi("t4", "a4");
		expectedTriple1p.addTeamApi("t5", "a5");

		Triple expectedTriple2q = new Triple("s2", "q", "o2");
		expectedTriple1p.addTeamApi("t6", "a6");

		OppositeTriples ot1 = new OppositeTriples(expectedTriple1p, expectedTriple1q);
		OppositeTriples ot2 = new OppositeTriples(expectedTriple1q, expectedTriple1p);
		OppositeTriples ot3 = new OppositeTriples(expectedTriple2p, expectedTriple2q);

		assertEquals(ot1.hashCode(), ot2.hashCode());
		assertNotEquals(ot1.hashCode(), ot3.hashCode());
	}

	@Test
	public void testOppositeTriplesEquals() {
		Triple expectedTriple1p = new Triple("s1", "p", "o1");
		expectedTriple1p.addTeamApi("t1", "a1");

		Triple expectedTriple1q = new Triple("s1", "q", "o1");
		expectedTriple1p.addTeamApi("t2", "a2");

		Triple expectedTriple2p = new Triple("s2", "p", "o2");
		expectedTriple1p.addTeamApi("t4", "a4");
		expectedTriple1p.addTeamApi("t5", "a5");

		Triple expectedTriple2q = new Triple("s2", "q", "o2");
		expectedTriple1p.addTeamApi("t6", "a6");

		OppositeTriples ot1 = new OppositeTriples(expectedTriple1p, expectedTriple1q);
		OppositeTriples ot2 = new OppositeTriples(expectedTriple1q, expectedTriple1p);
		OppositeTriples ot3 = new OppositeTriples(expectedTriple2p, expectedTriple2q);

		assertEquals(ot1, ot2);
		assertNotEquals(ot1, ot3);
	}

	@Test
	public void testMatchOppositeTriples() {

		Map<String, String> oppositePredicatesMap = new HashMap<String, String>();
		oppositePredicatesMap.put("p1", "q1");
		oppositePredicatesMap.put("p2", "q2");
		oppositePredicatesMap.put("p3", "q3");

		Map<String, Set<Triple>> tripleMap = new HashMap<String, Set<Triple>>();

		String key1 = "s1_o1";
		String key2 = "s2_o2";

		Triple s1_p1_o1 = new Triple("s1", "p1", "o1");
		s1_p1_o1.addTeamApi("t1", "a1");
		Triple s1_q1_o1 = new Triple("s1", "q1", "o1");
		s1_q1_o1.addTeamApi("t2", "a2");
		Triple s1_z_o1 = new Triple("s1", "z", "o1");
		s1_z_o1.addTeamApi("t3", "a3");

		Triple s2_p2_o2 = new Triple("s2", "p2", "o2");
		s2_p2_o2.addTeamApi("t4", "a4");
		s2_p2_o2.addTeamApi("t5", "a5");

		Triple s2_q2_o2 = new Triple("s2", "q2", "o2");
		s2_q2_o2.addTeamApi("t6", "a6");

		Set<Triple> s1o1Triples = new HashSet<Triple>(Arrays.asList(s1_p1_o1, s1_q1_o1, s1_z_o1));
		Set<Triple> s2o2Triples = new HashSet<Triple>(Arrays.asList(s2_p2_o2, s2_q2_o2));

		tripleMap.put(key1, s1o1Triples);
		tripleMap.put(key2, s2o2Triples);

		Set<OppositeTriples> oppositeTriples = new HashSet<OppositeTriples>(
				MetaKgOppositeTripleExtractor.matchOppositeTriples(oppositePredicatesMap, tripleMap));

		Triple expectedTriple1p = new Triple("s1", "p1", "o1");
		expectedTriple1p.addTeamApi("t1", "a1");

		Triple expectedTriple1q = new Triple("s1", "q1", "o1");
		expectedTriple1q.addTeamApi("t2", "a2");

		Triple expectedTriple2p = new Triple("s2", "p2", "o2");
		expectedTriple2p.addTeamApi("t4", "a4");
		expectedTriple2p.addTeamApi("t5", "a5");

		Triple expectedTriple2q = new Triple("s2", "q2", "o2");
		expectedTriple2q.addTeamApi("t6", "a6");

		Set<OppositeTriples> expectedOppositeTriples = new HashSet<OppositeTriples>();
		expectedOppositeTriples.add(new OppositeTriples(expectedTriple1p, expectedTriple1q));
		expectedOppositeTriples.add(new OppositeTriples(expectedTriple2p, expectedTriple2q));

		assertEquals(expectedOppositeTriples, oppositeTriples);

	}

}
