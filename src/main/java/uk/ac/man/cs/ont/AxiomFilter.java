package uk.ac.man.cs.ont;

import uk.ac.man.cs.util.*;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.model.*; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.util.*;
import java.util.stream.*;

/**
 * Created by chris on 16/07/19.
 */

public class AxiomFilter {

    private OWLDataFactory factory;
    private OWLReasoner reasoner;
    private HashSet<OWLAxiom> axioms;

    public AxiomFilter(Set<OWLAxiom> as) throws Exception {

        this.factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.axioms = new HashSet<>();
        this.axioms.addAll(as);
    }

    public Set<OWLAxiom> getAxioms(){
        return this.axioms; 
    }

    public void setAxioms(Set<OWLAxiom> as){
        this.axioms.clear();
        this.axioms.addAll(as); 
    }

    public void retain(Set<OWLAxiom> as){
        this.axioms.retainAll(as);
    }

    public void remove(Set<OWLAxiom> as){
        this.axioms.removeAll(as);
    } 

    public void removeTautologies(){ 
        HashSet<OWLAxiom> toBeRemoved = new HashSet<>();
        for(OWLAxiom a : axioms){ 
            boolean tautology = false;
            if(subsumedByTop(a))
                tautology = true;
            if(bottomSubsumedBy(a))
                tautology = true; 
            if(tautology)
                toBeRemoved.add(a);
        }

        this.axioms.removeAll(toBeRemoved);
    }

    private boolean subsumedByTop(OWLAxiom a){
        if(a instanceof OWLSubClassOfAxiom){
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(superclass.isTopEntity())
                return true;
        } 
        return false; 
    }

    private boolean bottomSubsumedBy(OWLAxiom a){
        if(a instanceof OWLSubClassOfAxiom){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            if(subclass.isBottomEntity())
                return true;
        } 
        return false; 
    }
}
