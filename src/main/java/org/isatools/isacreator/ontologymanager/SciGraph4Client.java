package org.isatools.isacreator.ontologymanager;

import org.apache.commons.lang.StringUtils;
import org.isatools.isacreator.configuration.Ontology;
import org.isatools.isacreator.configuration.RecommendedOntology;
import org.isatools.isacreator.ontologymanager.scigraph.io.AcceptedOntologies;
import org.isatools.isacreator.ontologymanager.scigraph.jsonresulthandlers.SciGraphSearchResultHandler;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

import java.util.*;

/**
 * SciGraph4Client searches terms across RBSDB ontologies
 *
 */
public class SciGraph4Client implements OntologyService {

    private SciGraphSearchResultHandler handler;

    private Map<String, Map<String, OntologyTerm>> cachedNodeChildrenQueries;

    public SciGraph4Client() {
        this.handler = new SciGraphSearchResultHandler();
        this.cachedNodeChildrenQueries = new HashMap<String, Map<String, OntologyTerm>>();
    }

    public Map<String, String> getOntologyNames() {
        return AcceptedOntologies.getOntologySourceToNames();
    }

    /**
     * Retrieves an OntologyTerm given the URI of a term and the ontology bioPortal URI
     *
     * @param termId
     * @param ontology
     * @return
     */
    public OntologyTerm getTerm(String termId, String ontology) {
        return handler.getTermMetadata(termId, ontology);
    }

    public Map<String, List<OntologyTerm>> exactSearch(String term, String ontology) {
        return handler.getSearchResults(term, ontology, null, true);
    }

    public Map<String, String> getTermMetadata(String termId, String ontology) {
        return handler.getTermMetadata(termId, ontology).getComments();
    }

    public Map<OntologySourceRefObject, List<OntologyTerm>> getTermsByPartialNameFromSource(String term, String source, boolean reverseOrder) {

        term = correctTermForHTTPTransport(term);

        Map<String, List<OntologyTerm>> searchResult = handler.getSearchResults(term, source, null);

        return convertStringKeyMapToOntologySourceRefKeyMap(searchResult);
    }

    public Map<OntologySourceRefObject, List<OntologyTerm>> getTermsByPartialNameFromSource(String term, List<RecommendedOntology> ontologies) {
        term = correctTermForHTTPTransport(term);

        Map<OntologySourceRefObject, List<OntologyTerm>> result = new HashMap<OntologySourceRefObject, List<OntologyTerm>>();

        for (RecommendedOntology ontology : ontologies) {
            if (ontology.getOntology() != null) {

                String subtree = null;
                if (ontology.getBranchToSearchUnder() != null && StringUtils.isNotEmpty(ontology.getBranchToSearchUnder().getBranchIdentifier())) {
                    subtree = ontology.getBranchToSearchUnder().getBranchIdentifier();
                }

                Map<String, List<OntologyTerm>> searchResult = handler.getSearchResults(term, ontology.getOntology().getOntologyAbbreviation(), subtree);

                if (searchResult != null) {
                    result.putAll(convertStringKeyMapToOntologySourceRefKeyMap(searchResult));
                }
            }
        }
        return result;
    }

    public Map<String, String> getOntologyVersions() {

        return AcceptedOntologies.getOntologySourceToVersion();
    }

    public Map<String, OntologyTerm> getOntologyRoots(String ontologyAbbreviation) {
        // http://data.bioontology.org/ontologies/EFO/classes/roots
        if (!cachedNodeChildrenQueries.containsKey(ontologyAbbreviation)) {
            cachedNodeChildrenQueries.put(ontologyAbbreviation, handler.getOntologyRoots(ontologyAbbreviation));
        }
        return cachedNodeChildrenQueries.get(ontologyAbbreviation);
    }

    public Map<String, OntologyTerm> getTermParent(String termAccession, String ontology) {

        return null;
    }

    public Map<String, OntologyTerm> getTermChildren(String termAccession, String ontologyAbbreviation) {
        String uniqueReferenceId = ontologyAbbreviation + "-" + termAccession + "-children";
        if (!cachedNodeChildrenQueries.containsKey(uniqueReferenceId)) {
            cachedNodeChildrenQueries.put(uniqueReferenceId, handler.getTermChildren(termAccession, ontologyAbbreviation));
        }
        return cachedNodeChildrenQueries.get(uniqueReferenceId);
    }

    public Map<String, OntologyTerm> getAllTermParents(String termAccession, String ontologyAbbreviation) {

        String uniqueReferenceId = ontologyAbbreviation + "-" + termAccession + "-parents";
        if (!cachedNodeChildrenQueries.containsKey(uniqueReferenceId)) {
            cachedNodeChildrenQueries.put(uniqueReferenceId, handler.getTermParents(termAccession, ontologyAbbreviation));
        }
        return cachedNodeChildrenQueries.get(uniqueReferenceId);

    }

    public Collection<Ontology> getAllOntologies() {
        return handler.getAllOntologies().values();
    }

    private Map<OntologySourceRefObject, List<OntologyTerm>> convertStringKeyMapToOntologySourceRefKeyMap(
            Map<String, List<OntologyTerm>> toConvert) {

        Map<OntologySourceRefObject, List<OntologyTerm>> convertedMap = new HashMap<OntologySourceRefObject, List<OntologyTerm>>();
        for (String ontologyId : toConvert.keySet()) {
            Ontology ontology = AcceptedOntologies.getAcceptedOntologies().get(ontologyId);
            if (ontology != null) {
                OntologySourceRefObject obj = new OntologySourceRefObject(ontology.getOntologyAbbreviation(),
                        ontologyId, ontology.getOntologyVersion(), ontology.getOntologyDisplayLabel());

                convertedMap.put(obj, new ArrayList<OntologyTerm>());

                for (OntologyTerm ontologyTerm : toConvert.get(ontologyId)) {
                    ontologyTerm.setOntologySourceInformation(obj);
                    convertedMap.get(obj).add(ontologyTerm);
                }

            }
        }
        return convertedMap;
    }

    private String correctTermForHTTPTransport(String term) {
        return term;
    }
}
