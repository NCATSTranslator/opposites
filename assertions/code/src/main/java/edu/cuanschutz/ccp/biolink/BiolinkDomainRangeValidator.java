package edu.cuanschutz.ccp.biolink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.cuanschutz.ccp.metakg.MetaKgParser.Triple;
import edu.cuanschutz.ccp.opposites.MetaKgOppositeAssertionExtractorMain;
import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import owltools.graph.OWLGraphWrapper;

/**
 * Validates a triple based on predicate domain/range compliance
 *
 */
public class BiolinkDomainRangeValidator {

	private static final Logger logger = Logger.getLogger(BiolinkDomainRangeValidator.class);

	private static final String BIOLINK_URL = "https://raw.githubusercontent.com/biolink/biolink-model/master/biolink-model.owl.ttl";
	protected static final String BIOLINK_NS = "https://w3id.org/biolink/vocab/";
	private static final String BIOLINK_MIXIN = "https://w3id.org/linkml/mixin";

	private OWLGraphWrapper graph;
	private OWLOntology ont;
	/* map predicate IRI to domain class(es) */
	private Map<String, Set<OWLClass>> predicateIriToDomainMap;
	/* map predicate IRI to range class(es) */
	private Map<String, Set<OWLClass>> predicateIriToRangeMap;
	/* catalog the set of available biolink predicates */
	private Set<String> validPredicateIris;

	public BiolinkDomainRangeValidator(InputStream biolinkStream, File propMissingDomainRangeReportFile)
			throws OWLOntologyCreationException, IOException {
		OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
		ont = ontologyManager.loadOntologyFromOntologyDocument(biolinkStream);
		graph = new OWLGraphWrapper(ont);
		loadDomainAndRangeMaps(propMissingDomainRangeReportFile);

	}

	public BiolinkDomainRangeValidator(File propMissingDomainRangeReportFile)
			throws OWLOntologyCreationException, IOException {
		this(new URL(BIOLINK_URL).openStream(), propMissingDomainRangeReportFile);
	}

	private void loadDomainAndRangeMaps(File propMissingDomainRangeReportFile) throws IOException {
		predicateIriToDomainMap = new HashMap<String, Set<OWLClass>>();
		predicateIriToRangeMap = new HashMap<String, Set<OWLClass>>();

		validPredicateIris = new HashSet<String>();
		Set<OWLObjectProperty> propMissingDomain = new HashSet<OWLObjectProperty>();
		Set<OWLObjectProperty> propMissingRange = new HashSet<OWLObjectProperty>();

		for (OWLObjectProperty prop : ont.getObjectPropertiesInSignature()) {

			String iri = prop.getIRI().toString();
			validPredicateIris.add(iri);
			Set<OWLClassExpression> domains = prop.getDomains(ont);
			Set<OWLClassExpression> ranges = prop.getRanges(ont);

			if (domains == null || domains.isEmpty()) {
				propMissingDomain.add(prop);
			} else {
				for (OWLClassExpression domain : domains) {
					if (domain instanceof OWLClass) {
						CollectionsUtil.addToOne2ManyUniqueMap(iri, (OWLClass) domain, predicateIriToDomainMap);
					}
				}
			}

			if (ranges == null || ranges.isEmpty()) {
				propMissingRange.add(prop);
			} else {
				for (OWLClassExpression range : ranges) {
					if (range instanceof OWLClass) {
						CollectionsUtil.addToOne2ManyUniqueMap(iri, (OWLClass) range, predicateIriToRangeMap);
					}
				}
			}
		}

		System.out.println("Writing missing domain/range report");
		/*
		 * output the missing domain/range report - this file catalogs properties in
		 * Biolink that are missing domains and/or ranges.
		 */
		List<String> sortedPropsMissingDomain = getIriStrings(propMissingDomain);
		Collections.sort(sortedPropsMissingDomain);
		List<String> sortedPropsMissingRange = getIriStrings(propMissingRange);
		Collections.sort(sortedPropsMissingRange);

		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(propMissingDomainRangeReportFile)) {
			for (String missingDomain : sortedPropsMissingDomain) {
				writer.write(String.format("MISSING DOMAIN:\t%s\n", missingDomain));
			}
			for (String missingRange : sortedPropsMissingRange) {
				writer.write(String.format("MISSING RANGE:\t%s\n", missingRange));
			}
		}

	}

	public boolean isValidDomain(String predicate, String subject) {
		OWLClass subjectCls = graph.getOWLClassByIdentifier(subject);
		if (subjectCls == null) {
			logger.warn("Class not found in Biolink: " + subject);
			return false;
		}
		Set<OWLClass> domains = predicateIriToDomainMap.get(predicate);
		boolean validDomain = hasAncestor(subjectCls, domains);
		return validDomain;
	}

	public boolean isValidRange(String predicate, String object) {
		OWLClass objectCls = graph.getOWLClassByIdentifier(object);
		if (objectCls == null) {
			logger.warn("Class not found in Biolink: " + object);
			return false;
		}
		Set<OWLClass> ranges = predicateIriToRangeMap.get(predicate);
		boolean validRange = hasAncestor(objectCls, ranges);
		return validRange;
	}

	/**
	 * @param cls
	 * @param possibleAncestors
	 * @return true if the input cls has at least one ancestor in possibleAncestors
	 */
	private boolean hasAncestor(OWLClass cls, Set<OWLClass> possibleAncestors) {
		Set<OWLClass> ancestors = graph.getAncestorsThroughIsA(cls);
		ancestors.add(cls);

		for (OWLClass ancestor : ancestors) {
			if (possibleAncestors.contains(ancestor)) {
				return true;
			}
		}
		return false;
	}

	public void close() throws IOException {
		if (graph != null) {
			graph.close();
		}
	}

	public void validate(Triple triple, BufferedWriter writer) throws IOException {
		String sub = triple.getSubject();
		String pred = triple.getPredicate();
		String obj = triple.getObject();

		String relation = String.format("%s ----> %s ----> %s", sub, pred, obj);

		sub = (!sub.startsWith(BIOLINK_NS)) ? BIOLINK_NS + sub : sub;
		pred = (!pred.startsWith(BIOLINK_NS)) ? BIOLINK_NS + pred : pred;
		obj = (!obj.startsWith(BIOLINK_NS)) ? BIOLINK_NS + obj : obj;

		if (validPredicateIris.contains(pred)) {
			validateDomain(triple, writer, sub, pred, relation);
			validateRange(triple, writer, pred, obj, relation);
		} else {
			writer.write(String.format(
					"%s\tInvalid Biolink predicate observed. '%s' is not part of the Biolink ontology. In '%s'\n",
					MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), pred, relation));
		}

	}

	protected void validateDomain(Triple triple, BufferedWriter writer, String sub, String pred, String relation)
			throws IOException {
		Set<OWLClass> domainClasses = predicateIriToDomainMap.get(pred);

		if (domainClasses != null) {
			List<String> domains = getIriStrings(domainClasses);
			Collections.sort(domains);

			OWLClass subjectCls = graph.getOWLClassByIdentifier(sub);
			if (subjectCls == null) {
				writer.write(String.format(
						"%s\tInvalid Biolink class observed. '%s' is not part of the Biolink ontology. In '%s'\n",
						MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), sub, relation));
			} else {
				String mixinName = "";
				List<String> subs = Arrays.asList(sub);
				if (isMixin(sub)) {
					mixinName = sub + "|";
					subs = expandMixins(sub);
				}

				for (String s : subs) {
					if (!isValidDomain(pred, s)) {
						writer.write(String.format("%s\tInvalid domain detected. Subject: %s%s is not %s %s. In '%s'\n",
								MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), mixinName, s,
								(domains.size() > 1 ? "one of" : "a"), domains.toString(), relation));
					}
				}
			}
		} else {
			/*
			 * make a note that the domain cannot be validated because the predicate being
			 * used does not specify a domain in the Biolink ontology
			 */
			writer.write(String.format(
					"%s\tUnable to validate domain '%s' for predicate '%s'. The Biolink ontology does not specify a domain for this predicate. In '%s'\n",
					MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), sub, pred, relation));
		}
	}

	protected void validateRange(Triple triple, BufferedWriter writer, String pred, String obj, String relation)
			throws IOException {
		Set<OWLClass> rangeClasses = predicateIriToRangeMap.get(pred);
		if (rangeClasses != null) {
			List<String> ranges = getIriStrings(rangeClasses);
			Collections.sort(ranges);

			OWLClass objectCls = graph.getOWLClassByIdentifier(obj);
			if (objectCls == null) {
				writer.write(String.format(
						"%s\tInvalid Biolink class observed. '%s' is not part of the Biolink ontology. In '%s'\n",
						MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), obj, relation));
			} else {
				String mixinName = "";
				List<String> objs = Arrays.asList(obj);
				if (isMixin(obj)) {
					mixinName = obj + "|";
					objs = expandMixins(obj);
				}

				for (String o : objs) {
					if (!isValidRange(pred, o)) {
						writer.write(String.format("%s\tInvalid range detected. Object: %s%s is not %s %s. In '%s'\n",
								MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), mixinName, o,
								(ranges.size() > 1 ? "one of" : "a"), ranges.toString(), relation));
					}
				}
			}

		} else {
			/*
			 * make a note that the range cannot be validated because the predicate being
			 * used does not specify a range in the Biolink ontology
			 */
			writer.write(String.format(
					"%s\tUnable to validate range '%s' for predicate '%s'. The Biolink ontology does not specify a range for this predicate. In '%s'\n",
					MetaKgOppositeAssertionExtractorMain.getTeamApiString(triple), obj, pred, relation));
		}
	}

	/**
	 * @param mixinId
	 * @return all subclasses of the mixin, excluding any subclasses that are
	 *         themselves mixins
	 */
	protected List<String> expandMixins(String mixinId) {
		OWLClass mixinInstance = graph.getOWLClassByIdentifier(mixinId);
		if (mixinInstance == null) {
			throw new IllegalArgumentException("Null mixin class for id: " + mixinId);
		}
		Set<OWLClass> descendants = graph.getDescendantsThroughIsA(mixinInstance);
		List<String> descendantIris = new ArrayList<String>();
		for (OWLClass d : descendants) {
			if (!isMixin(d.getIRI().toString())) {
				descendantIris.add(d.getIRI().toString());
			}
		}
		Collections.sort(descendantIris);
		return descendantIris;
	}

	/**
	 * @param id
	 * @return true if the class referenced by the input id is a mixin
	 */
	protected boolean isMixin(String id) {
		OWLClass cls = graph.getOWLClassByIdentifier(id);
		if (cls == null) {
			throw new IllegalArgumentException("Null class for id: " + id);
		}

		Set<OWLClassExpression> superClassExpressions = cls.getSuperClasses(ont);
		for (OWLClassExpression oce : superClassExpressions) {
			if (oce instanceof OWLClass) {
				if (((OWLClass) oce).getIRI().toString().equals(BIOLINK_MIXIN)) {
					return true;
				}
			}
		}

		return false;
	}

	private List<String> getIriStrings(Set<? extends HasIRI> classes) {
		List<String> iris = new ArrayList<String>();
		for (HasIRI cls : classes) {
			iris.add(cls.getIRI().toString());
		}
		return iris;
	}

}
