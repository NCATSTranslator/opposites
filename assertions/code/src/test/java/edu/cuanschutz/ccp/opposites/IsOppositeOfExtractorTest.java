package edu.cuanschutz.ccp.opposites;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import edu.cuanschutz.ccp.opposites.IsOppositeOfExtractor.OntConcept;

public class IsOppositeOfExtractorTest {

	@Test
	public void testEnsureBinaryOppositeRelationshipValid1() {
		Map<OntConcept, OntConcept> oppositesMap = new HashMap<OntConcept, OntConcept>();
		OntConcept keyConcept = new OntConcept("http://concept1", "concept1");
		OntConcept valueConcept = new OntConcept("http://concept2", "concept2");
		IsOppositeOfExtractor.ensureBinaryOppositeRelationship(oppositesMap, keyConcept, valueConcept);
		// this test passes unless an exception is thrown
		assertTrue(true);
	}

	@Test
	public void testEnsureBinaryOppositeRelationshipValid2() {
		Map<OntConcept, OntConcept> oppositesMap = new HashMap<OntConcept, OntConcept>();
		OntConcept keyConcept = new OntConcept("http://concept1", "concept1");
		OntConcept valueConcept = new OntConcept("http://concept2", "concept2");
		IsOppositeOfExtractor.ensureBinaryOppositeRelationship(oppositesMap, keyConcept, valueConcept);
		// this test passes unless an exception is thrown
		assertTrue(true);
	}

	@Test(expected = IllegalStateException.class)
	public void testEnsureBinaryOppositeRelationshipInValid() {
		Map<OntConcept, OntConcept> oppositesMap = new HashMap<OntConcept, OntConcept>();
		OntConcept keyConcept = new OntConcept("http://concept1", "concept1");
		OntConcept valueConcept = new OntConcept("http://concept2", "concept2");
		OntConcept valueConcept2 = new OntConcept("http://concept3", "concept3");
		oppositesMap.put(keyConcept, valueConcept2);
		IsOppositeOfExtractor.ensureBinaryOppositeRelationship(oppositesMap, keyConcept, valueConcept);
	}

}
