package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.*; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;
import java.util.stream.*;

public final class Usage { 


    public static Set<OWLAxiom> getLogicalAxioms(OWLOntology ont, boolean includeClosure, boolean includeAnnotations) {

            Set<OWLLogicalAxiom> logicalAxioms = ont.getLogicalAxioms(includeClosure);
            Set<OWLAxiom> axioms = new HashSet<>();  
            for(OWLLogicalAxiom axiom : logicalAxioms){
                if(!includeAnnotations){ 
                    axioms.add(((OWLAxiom) axiom).getAxiomWithoutAnnotations());
                } else {
                    axioms.add((OWLAxiom) axiom);
                }
            }
            return axioms;
    }

    public static Set<OWLAxiom> getLogicalAxioms(OWLOntology ont, boolean includeClosure) {
        Set<OWLLogicalAxiom> logicalAxioms = ont.getLogicalAxioms(includeClosure);
        Set<OWLAxiom> axioms = new HashSet<>();  
        for(OWLLogicalAxiom axiom : logicalAxioms){
            axioms.add((OWLAxiom) axiom);
        } 
        return axioms;
    }

    public static Set<OWLAxiom> entityUsage(OWLEntity entity, Set<OWLAxiom> axioms){
        Set<OWLAxiom> res = new HashSet<>(); 
        //for(OWLAxiom axiom : ontology.getAxioms(Imports.EXCLUDED)){
        for(OWLAxiom axiom : axioms){
            if((axiom.getSignature()).contains(entity)){
                    res.add(axiom);
            }
        }
        return res; 
    }

}
