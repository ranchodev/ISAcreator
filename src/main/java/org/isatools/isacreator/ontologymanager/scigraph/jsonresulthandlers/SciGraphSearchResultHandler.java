package org.isatools.isacreator.ontologymanager.scigraph.jsonresulthandlers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.isatools.isacreator.configuration.Ontology;
import org.isatools.isacreator.ontologymanager.OntologyManager;
import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
import org.isatools.isacreator.ontologymanager.scigraph.io.AcceptedOntologies;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SciGraphSearchResultHandler {

    public static final String SCIGRAPH_HTTP_DEFAULT = "http";
    public static final String SCIGRAPH_HOST_DEFAULT = "";
    public static final String SCIGRAPH_PORT_DEFAULT = "9000";

    public static final String PARENTS = "ancestors";
    public static final String CHILDREN = "children";

    public static final String SCIGRAPH_HOST = "scigraph.host";
    public static final String SCIGRAPH_PORT = "scigraph.port";

    public static final String SCIGRAPH = "/scigraph";

    public static final String PREFIXES = "prefixes";
    public static final String VOCABULARY = "vocabulary";
    public static final String SEARCH = "search";
    public static final String AUTOCOMPLETE = "autocomplete";

    public static final String CONCEPT = "concept";
    public static final String ALL = "all";
    public static final String LIMIT = "limit";
    public static final String SYNONYMS = "searchSynonyms";
    public static final String ABBREVIATIONS = "searchAbbreviations";
    public static final String ACRONYMS = "searchAcronyms";
    public static final String DEPRECATED = "includeDeprecated";
    public static final String PREFIX = "prefix";

    public Map<String, List<OntologyTerm>> getSearchResults(String term, String ontologyIds, String subtree) {
        return getSearchResults(term, ontologyIds, subtree, false);
    }

    /**
     * Returns the result of the search operation
     *
     * @param term        - the string being searched for
     * @param ontologyIds - the ontologies the search is being restricted to
     * @param @nullable   subtree - a subtree, if any to be searched under (optional)
     * @return - Map from the id of the ontology to the list of terms found under it.
     */
    public Map<String, List<OntologyTerm>> getSearchResults(String term, String ontologyIds, String subtree, boolean exactMatch) {

        // map from ontology id to the list of terms found for that id.
        Map<String, List<OntologyTerm>> result = new HashMap<String, List<OntologyTerm>>();

        String content = querySearchEndpoint(term, ontologyIds, subtree, exactMatch);

        JSONArray results = (JSONArray) JSONValue.parse(content);

        if (results == null)
            return result;

        for (Object obj : results) {

            JSONObject resultItem;

            if (((JSONObject) obj).containsKey(CONCEPT)) {
                resultItem = (JSONObject) ((JSONObject) obj).get(CONCEPT);
            } else {
                resultItem = (JSONObject) obj;
            }

            String ontologyId = extractOntologyId(resultItem);

            if (!result.containsKey(ontologyId)) {
                result.put(ontologyId, new ArrayList<OntologyTerm>());
            }
            OntologyTerm ontologyTerm = createOntologyTerm(resultItem);
            result.get(ontologyId).add(ontologyTerm);

        }

        return result;
    }

    private String extractOntologyId(JSONObject ontologyItemJsonDictionary) {
        String curie = (String) ontologyItemJsonDictionary.get("curie");
        return StringUtils.substringBefore(curie, ":");
    }

    private void extractDefinitionFromOntologyTerm(JSONObject ontologyItemJsonDictionary, OntologyTerm ontologyTerm) {

        return;
    }

    private void extractSynonymsFromOntologyTerm(JSONObject ontologyItemJsonDictionary, OntologyTerm ontologyTerm) {

        return;
    }


    private String querySearchEndpoint(String term, String ontologyIds, String subtree, boolean exactMatch) {
        try {
            HttpClient client = new HttpClient();

            GetMethod method = new GetMethod();

            method.setURI(new URI(SCIGRAPH_HTTP_DEFAULT, "", System.getProperty(SCIGRAPH_HOST, SCIGRAPH_HOST_DEFAULT),
                    Integer.valueOf(System.getProperty(SCIGRAPH_PORT, SCIGRAPH_PORT_DEFAULT))));

            method.setPath(StringUtils.join(new String[]{SCIGRAPH, VOCABULARY,
                    ALL.equalsIgnoreCase(ontologyIds) ? SEARCH : AUTOCOMPLETE, URIUtil.encodeQuery(term)}, "/"));

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new NameValuePair(LIMIT,
                    System.getProperty("scigraph.search.limit", "20")));
            params.add(new NameValuePair(SYNONYMS,
                    System.getProperty("scigraph.search.synonyms", "true")));
            params.add(new NameValuePair(ABBREVIATIONS,
                    System.getProperty("scigraph.search.abbreviations", "true")));
            params.add(new NameValuePair(ACRONYMS,
                    System.getProperty("scigraph.search.acronyms", "true")));
            params.add(new NameValuePair(DEPRECATED,
                    System.getProperty("scigraph.search.deprecated", "true")));

            if (!ALL.equalsIgnoreCase(ontologyIds)) {
                for (String s: StringUtils.split(ontologyIds, ",")) {
                    params.add(new NameValuePair(PREFIX, s));
                }
            }
            method.setQueryString(params.toArray(new NameValuePair[]{}));

            try {
                setHostConfiguration(client);
            } catch (Exception e) {
                System.err.println("Problem encountered setting host configuration for search");
            }

            long startTime = System.currentTimeMillis();
            int statusCode = client.executeMethod(method);
            if (statusCode != -1) {
                System.out.println("It took " + (System.currentTimeMillis() - startTime) + "ms to do that query...");
                String contents = method.getResponseBodyAsString();
                method.releaseConnection();
                return contents;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Map<String, Ontology> getAllOntologies() {

        Map<String, Ontology> result = new HashMap<String, Ontology>();
        String content = queryOntologyEndpoint();
        JSONArray obj = (JSONArray) JSONValue.parse(content);

        for (Object resultItem : obj) {
            addOntology(result, (String) resultItem);
        }

        return result;
    }

    private void addOntology(Map<String, Ontology> result, String resultItem) {

        Ontology newOntology = new Ontology(resultItem, "version", resultItem, resultItem);
        result.put(resultItem, newOntology);
    }


    public String queryOntologyEndpoint() {
        try {
            HttpClient client = new HttpClient();

            GetMethod method = new GetMethod();

            method.setURI(new URI(SCIGRAPH_HTTP_DEFAULT, "", System.getProperty(SCIGRAPH_HOST, SCIGRAPH_HOST_DEFAULT),
                    Integer.valueOf(System.getProperty(SCIGRAPH_PORT, SCIGRAPH_PORT_DEFAULT))));
            method.setPath(StringUtils.join(new String[]{SCIGRAPH, VOCABULARY, PREFIXES}, "/"));

            try {
                setHostConfiguration(client);
            } catch (Exception e) {
                System.err.println("Problem encountered setting host configuration for ontology search");
            }

            int statusCode = client.executeMethod(method);
            if (statusCode != -1) {
                String contents = method.getResponseBodyAsString();
                method.releaseConnection();
                return contents;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setHostConfiguration(HttpClient client) {
        HostConfiguration configuration = new HostConfiguration();
        configuration.setHost(System.getProperty(SCIGRAPH_HOST, SCIGRAPH_HOST_DEFAULT),
                Integer.valueOf(System.getProperty(SCIGRAPH_PORT, SCIGRAPH_PORT_DEFAULT)));
        String proxyPort = System.getProperty("http.proxyPort");
        if (proxyPort == null)
            return;
        configuration.setProxy(System.getProperty("http.proxyHost"), Integer.valueOf(System.getProperty("http.proxyPort")));
        client.setHostConfiguration(configuration);
    }

    public OntologyTerm getTermMetadata(String termId, String ontologyId) {

        String content = queryTermMetadataEndpoint(termId, ontologyId);
        JSONObject obj;


        Object json = JSONValue.parse(content);
        if (json instanceof JSONArray) {
            obj = (JSONObject) ((JSONArray) json).get(0);
        } else {
            obj = (JSONObject) json;
        }

        // if we have a nice error free page, continue
        if (!obj.containsKey("errors")) {
            String label = ((JSONArray) obj.get("labels")).get(0).toString(); //.split(",")[0];

            OntologyTerm ontologyTerm = new OntologyTerm(
                    StringUtils.substringBefore(obj.get("curie").toString(), ":") + ":" + label,
                    obj.get("iri").toString(),
                    obj.get("iri").toString(), null);
            ontologyTerm.addToComments("Service Provider", OntologyManager.SCI_GRAPH);
            ontologyTerm.addToComments("Label", label);
            ontologyTerm.addToComments("Curie", obj.get("curie").toString());
            extractDefinitionFromOntologyTerm(obj, ontologyTerm);
            extractSynonymsFromOntologyTerm(obj, ontologyTerm);

            return ontologyTerm;
        } else {
            return null;
        }
    }

    private OntologySourceRefObject getOntologySourceRefObject(String ontologyId) {
        Ontology associatedOntologySource = AcceptedOntologies.getAcceptedOntologies().get(ontologyId);
        return new OntologySourceRefObject(associatedOntologySource.getOntologyAbbreviation(), associatedOntologySource.getOntologyID(), associatedOntologySource.getOntologyVersion(), associatedOntologySource.getOntologyDisplayLabel());
    }

    public String queryTermMetadataEndpoint(String termId, String ontologyURI) {
        try {
            HttpClient client = new HttpClient();

            GetMethod method = new GetMethod();

            method.setURI(new URI(SCIGRAPH_HTTP_DEFAULT, "", System.getProperty(SCIGRAPH_HOST, SCIGRAPH_HOST_DEFAULT),
                    Integer.valueOf(System.getProperty(SCIGRAPH_PORT, SCIGRAPH_PORT_DEFAULT))));

            if (StringUtils.isEmpty(ontologyURI)) {
                method.setPath(StringUtils.join(new String[]{"/scigraph", "vocabulary",
                        "id", URLEncoder.encode(termId, "UTF-8")}, "/"));
            } else {
                method.setPath(StringUtils.join(new String[]{"/scigraph", "vocabulary",
                        "term", URIUtil.encodeQuery(termId)}, "/"));
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                if (!"all".equalsIgnoreCase(ontologyURI)) {
                    for (String s: StringUtils.split(ontologyURI, ",")) {
                        params.add(new NameValuePair("prefix", s));
                    }
                }
                method.setQueryString(params.toArray(new NameValuePair[]{}));
            }

            try {
                setHostConfiguration(client);
            } catch (Exception e) {
                System.err.println("Problem encountered setting host configuration for ontology search");
            }

            int statusCode = client.executeMethod(method);
            if (statusCode != -1) {
                String contents = method.getResponseBodyAsString();
                method.releaseConnection();
                return contents;
            }
        } catch (Exception e) {
            System.err.println("Unable to retrieve term metadata");
        }
        return null;
    }

    public Map<String, OntologyTerm> getOntologyRoots(String ontologyAbbreviation) {

        return new HashMap<String, OntologyTerm>();
    }

    private OntologyTerm createOntologyTerm(JSONObject annotationItem) {

        String label = ((JSONArray) annotationItem.get("labels")).get(0).toString(); //.split(",")[0];

        OntologyTerm ontologyTerm = new OntologyTerm(
                StringUtils.substringBefore(annotationItem.get("curie").toString(), ":") + ":" + label,
                                                    annotationItem.get("iri").toString(),
                                                    annotationItem.get("iri").toString(),
                                                    null);
        ontologyTerm.addToComments("Label", label);
        ontologyTerm.addToComments("Curie", annotationItem.get("curie").toString());
        ontologyTerm.addToComments("Service Provider", OntologyManager.SCI_GRAPH);
        extractDefinitionFromOntologyTerm(annotationItem, ontologyTerm);
        extractSynonymsFromOntologyTerm(annotationItem, ontologyTerm);
        String ontologyId = extractOntologyId(annotationItem);
        if (ontologyId != null) {
            String ontologyAbbreviation = ontologyId.substring(ontologyId.lastIndexOf('/') + 1);
            OntologySourceRefObject sourceRefObject = OntologyManager.getOntologySourceReferenceObjectByAbbreviation(ontologyId);
            if (sourceRefObject != null)
                ontologyTerm.setOntologySourceInformation(sourceRefObject);
        }
        return ontologyTerm;
    }

    /**
     * @param url
     * @return
     */
    private String generalQueryEndpoint(String url) {
        try {
            HttpClient client = new HttpClient();

            GetMethod method = new GetMethod(url);
            try {
                setHostConfiguration(client);
            } catch (Exception e) {
                System.err.println("Problem encountered setting host configuration for ontology search");
            }

            int statusCode = client.executeMethod(method);
            if (statusCode != -1) {
                String contents = method.getResponseBodyAsString();
                method.releaseConnection();
                return contents;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Map<String, OntologyTerm> getTermParents(String termAccession, String ontologyAbbreviation) {
        return getTermChildrenOrParents(termAccession, ontologyAbbreviation, PARENTS);
    }

    public Map<String, OntologyTerm> getTermChildren(String termAccession, String ontologyAbbreviation) {
        return getTermChildrenOrParents(termAccession, ontologyAbbreviation, CHILDREN);
    }

    /**
     * Will make a call to get the parents or children of a term, identified by its termAccession in a particular
     * ontology, defined by the ontologyAbbreviation.
     *
     * @param termAccession        - e.g. http://purl.obolibrary.org/obo/OBI_0000785
     * @param ontologyAbbreviation - e.g. EFO
     * @param parentsOrChildren    - 'parents' or 'children' as an input value
     * @return Map from the ontology term id to its OntologyTerm object.
     */
    public Map<String, OntologyTerm> getTermChildrenOrParents(String termAccession, String ontologyAbbreviation, String parentsOrChildren) {

        return new ListOrderedMap<String, OntologyTerm>();
    }
}
