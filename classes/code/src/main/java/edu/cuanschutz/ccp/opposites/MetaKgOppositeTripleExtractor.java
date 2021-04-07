package edu.cuanschutz.ccp.opposites;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cuanschutz.ccp.metakg.MetaKgParser;
import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import lombok.Data;

/**
 * Extracts pairs of subject-predicate-object triples that share the same
 * subject and object, but have predicates that are opposite in nature. Opposite
 * predicates are provided by the oppositePredicatesMap argument. The MetaKG is
 * downloaded automatically whenever the code is executed.
 *
 */
public class MetaKgOppositeTripleExtractor {

	private static final Logger logger = Logger.getLogger(MetaKgOppositeTripleExtractor.class);

	public static Collection<OppositeTriples> extractOppositeMetaKgTriples(Map<String, String> oppositePredicatesMap)
			throws IOException {

		InputStreamReader isr = null;
		try {
			URL metaKgUrl = new URL(MetaKgParser.META_KG_URL);
			isr = new InputStreamReader(metaKgUrl.openStream());
		} catch (IOException e) {
			logger.error(String.format("MetaKG appears to be unavailable. Analysis cannot continue.\n%s %s\nExiting...",
					e.getClass().getName(), e.getMessage()));
			System.exit(-1);
		}

		/*
		 * tripleMap; key = subjectId_objectId, value = Triples that use the subjectId
		 * and objectId in the key
		 */
		Map<String, Set<Triple>> tripleMap = MetaKgParser.parseMetaKgJson(isr);

		return matchOppositeTriples(oppositePredicatesMap, tripleMap);
	}

	/**
	 * @param oppositePredicatesMap
	 * @param tripleMap             key = subjectId_objectId, value = Triples that
	 *                              use the subjectId and objectId in the key
	 * @return
	 */
	protected static Collection<OppositeTriples> matchOppositeTriples(Map<String, String> oppositePredicatesMap,
			Map<String, Set<Triple>> tripleMap) {
		List<OppositeTriples> oppositeTriples = new ArrayList<OppositeTriples>();
		for (Entry<String, Set<Triple>> entry : tripleMap.entrySet()) {
			// group triples by predicate
			Map<String, Set<Triple>> predicateToTriplesMap = new HashMap<String, Set<Triple>>();
			for (Triple t : entry.getValue()) {
				CollectionsUtil.addToOne2ManyUniqueMap(t.getPredicate(), t, predicateToTriplesMap);
			}

			// look for opposite triples
			for (Entry<String, String> oppEntry : oppositePredicatesMap.entrySet()) {
				if (predicateToTriplesMap.containsKey(oppEntry.getKey())
						&& predicateToTriplesMap.containsKey(oppEntry.getValue())) {
					// then this subject/object pair has opposite triples

					Set<Triple> set1 = predicateToTriplesMap.get(oppEntry.getKey());
					Set<Triple> set2 = predicateToTriplesMap.get(oppEntry.getValue());

					Triple t1 = combineTriples(set1);
					Triple t2 = combineTriples(set2);

					oppositeTriples.add(new OppositeTriples(t1, t2));
				}
			}
		}
		return oppositeTriples;
	}

	/**
	 * Combines triples with the same subject/pred/object by combining the team
	 * names and apis
	 * 
	 * @param set1
	 * @return
	 */
	protected static Triple combineTriples(Set<Triple> triples) {
		Triple t = triples.iterator().next();
		triples.remove(t);

		for (Triple triple : triples) {
			if (triple.getSubject().equals(t.getSubject()) && triple.getPredicate().equals(t.getPredicate())
					&& triple.getObject().contentEquals(t.getObject())) {
				for (Entry<String, String> entry : triple.getTeamToApiMap().entrySet()) {
					t.addTeamApi(entry.getKey(), entry.getValue());
				}
			} else {
				throw new IllegalArgumentException("Cannot combine triples with different s-p-o");
			}
		}

		return t;

	}

	@Data
	public static class OppositeTriples {
		private final Triple t1;
		private final Triple t2;

		/**
		 * Equal if o1.t1 == o2.t1 && o1.t2 == o2.t2 OR o1.t1 == o2.t2 && o1.t2 == o2.t1
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OppositeTriples other = (OppositeTriples) obj;

			if (t1 == null) {
				if (other.t1 != null)
					return false;
			}
			if (t2 == null) {
				if (other.t2 != null)
					return false;
			}

			if ((t1.equals(other.t1) && t2.equals(other.t2)) || (t1.equals(other.t2) && (t2.equals(other.t1)))) {
				return true;
			}
			return false;
		}

		/**
		 * ordering of t1 & t2 doesn't matter
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;

			if (t1.hashCode() > t2.hashCode()) {
				result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
				result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
			} else {
				result = prime * result + ((t2 == null) ? 0 : t2.hashCode());
				result = prime * result + ((t1 == null) ? 0 : t1.hashCode());
			}
			return result;
		}

	}

}
