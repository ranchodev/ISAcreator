package org.isatools.isacreator.ontologymanager.scigraph.model;

import org.isatools.isacreator.configuration.Ontology;
import org.isatools.isacreator.ontologymanager.common.OntologyTerm;

/**
 * Created by the ISA team
 *
 */
public class AnnotatorResult {

    private OntologyTerm ontologyTerm;
    private Ontology ontologySource;

    private int score;
    private int startIndex;
    private int endIndex;

    private ScoringConfidence confidenceLevel = ScoringConfidence.MEDIUM;

    public AnnotatorResult(OntologyTerm ontologyTerm, Ontology ontologySource, int score, int startIndex, int endIndex) {
        this.ontologyTerm = ontologyTerm;
        this.ontologySource = ontologySource;
        this.score = score;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public OntologyTerm getOntologyTerm() {
        return ontologyTerm;
    }

    public Ontology getOntologySource() {
        return ontologySource;
    }

    public int getScore() {
        return score;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public ScoringConfidence getScoringConfidenceLevel() {
        return confidenceLevel;
    }

    public void setScoringConfidence(ScoringConfidence confidence) {
        this.confidenceLevel = confidence;
    }

    @Override
    public String toString() {
        return ontologyTerm.getOntologyTermName() + " (" + ontologyTerm.getOntologyTermAccession() + ")";
    }
}
