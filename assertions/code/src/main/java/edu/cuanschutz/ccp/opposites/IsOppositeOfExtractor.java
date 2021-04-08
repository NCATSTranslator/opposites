package edu.cuanschutz.ccp.opposites;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.tools.ant.util.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import lombok.Data;
import owltools.graph.OWLGraphWrapper;

/**
 * Processes an ontology file and extracts pairs of classes that are linked
 * using RO:is_opposite_of (http://purl.obolibrary.org/obo/RO_0002604)
 *
 */
public class IsOppositeOfExtractor {

	private static final IRI IS_OPPOSITE_OF_IRI = IRI.create("http://purl.obolibrary.org/obo/RO_0002604");

	private static final Logger LOGGER = Logger.getLogger(IsOppositeOfExtractor.class);

	/**
	 * Given an ontology, extract uses of the RO:is_opposite_of property
	 * 
	 * @param ontologyStream
	 * @return a mapping between ontology concepts that are linked using the
	 *         RO:is_opposite_of property
	 * @throws OWLOntologyCreationException
	 * @throws IOException
	 */
	public static Map<OntConcept, OntConcept> extractOppositeClasses(InputStream ontologyStream)
			throws OWLOntologyCreationException, IOException {
		Map<OntConcept, OntConcept> oppositesMap = new HashMap<OntConcept, OntConcept>();

		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = ontologyManager.loadOntologyFromOntologyDocument(ontologyStream);
		try (OWLGraphWrapper graph = new OWLGraphWrapper(ont)) {
			OWLAnnotationProperty isOppositeOfAnnotationProperty = graph.getOWLAnnotationProperty(IS_OPPOSITE_OF_IRI);

			/*
			 * There is probably a more efficient way to do this, but for now we cycle
			 * through each class in the ontology and check its axioms for use of the
			 * RO:is_opposite_of property
			 */
			for (OWLClass owlClass : graph.getAllOWLClasses()) {
				Set<OWLAnnotationAssertionAxiom> annotationAssertionAxioms = owlClass.getAnnotationAssertionAxioms(ont);
				for (OWLAnnotationAssertionAxiom axiom : annotationAssertionAxioms) {
					processAxiom(oppositesMap, ont, graph, isOppositeOfAnnotationProperty, owlClass, axiom);
				}
			}

			return oppositesMap;
		}

	}

	/**
	 * Check to see if the axiom makes use of RO:is_opposite_of, store the opposite
	 * concept pair in the {@code oppositesMap} if a is_opposite_of relation is
	 * observed.
	 * 
	 * @param oppositesMap
	 * @param ont
	 * @param graph
	 * @param isOppositeOfAnnotationProperty
	 * @param owlClass
	 * @param axiom
	 */
	private static void processAxiom(Map<OntConcept, OntConcept> oppositesMap, OWLOntology ont, OWLGraphWrapper graph,
			OWLAnnotationProperty isOppositeOfAnnotationProperty, OWLClass owlClass,
			OWLAnnotationAssertionAxiom axiom) {
		OWLAnnotation annotation = axiom.getAnnotation();
		OWLAnnotationProperty property = annotation.getProperty();
		if (property.equals(isOppositeOfAnnotationProperty)) {
			OWLAnnotationValue value = annotation.getValue();
			if (value instanceof IRI) {
				String oppositeClsIri = ((IRI) value).toString();
				List<String> sortedClasses = Arrays.asList(owlClass.getIRI().toString(), oppositeClsIri);
				Collections.sort(sortedClasses);
				assert sortedClasses.size() == 2;
				String keyClsIri = sortedClasses.get(0);
				String valueClsIri = sortedClasses.get(1);

				String keyClsLabel = getLabel(ont, graph.getOWLClass(keyClsIri));
				String valueClsLabel = getLabel(ont, graph.getOWLClass(valueClsIri));

				OntConcept keyConcept = new OntConcept(keyClsIri, keyClsLabel);
				OntConcept valueConcept = new OntConcept(valueClsIri, valueClsLabel);

				ensureBinaryOppositeRelationship(oppositesMap, keyConcept, valueConcept);
				oppositesMap.put(keyConcept, valueConcept);
			} else {
				LOGGER.warn("Expected IRI but observed: " + value.getClass().getName() + " -- " + value.toString());
			}
		}
	}

	/**
	 * make sure a given class doesn't have more than one opposite b/c the current
	 * logic only handles a single opposite. Don't think this is likely, but
	 * checking just in case.
	 * 
	 * @param oppositesMap
	 * @param keyConcept
	 * @param valueConcept
	 */
	protected static void ensureBinaryOppositeRelationship(Map<OntConcept, OntConcept> oppositesMap,
			OntConcept keyConcept, OntConcept valueConcept) {
		if (oppositesMap.containsKey(keyConcept) && !oppositesMap.get(keyConcept).equals(valueConcept)) {
			throw new IllegalStateException(
					String.format("Duplicate key detected -- this class has multiple opposites? %s -- %s & %s",
							keyConcept.toString(), oppositesMap.get(keyConcept), valueConcept));
		}
	}

	private static String getLabel(OWLOntology ont, OWLClass cls) {
		if (cls != null && ont != null) {
			Set<OWLAnnotation> annotations = cls.getAnnotations(ont);
			for (OWLAnnotation annotation : annotations) {
				if (annotation.getProperty().isLabel()) {
					String s = annotation.getValue().toString();
					s = StringUtils.removePrefix(s, "\"");
					s = StringUtils.removeSuffix(s, "\"");
					s = StringUtils.removeSuffix(s, "\"^^xsd:string");
					s = StringUtils.removeSuffix(s, "\"@en");
					return s;
				}
			}
		}

		return null;
	}

	@Data
	public static class OntConcept {
		private final String iri;
		private final String label;

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OntConcept other = (OntConcept) obj;
			if (iri == null) {
				if (other.iri != null)
					return false;
			} else if (!iri.equals(other.iri))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((iri == null) ? 0 : iri.hashCode());
			return result;
		}

	}

}
