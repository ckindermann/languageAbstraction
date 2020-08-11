package uk.ac.man.cs.regularities.axiomsets;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.cep.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.HermiT.ReasonerFactory;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.*;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.change.AxiomChangeData;
import org.semanticweb.owlapi.search.EntitySearcher; 

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class ClassFrame {

    private OWLClassExpression entity;
    private Set<OWLSubClassOfAxiom> superclasses;
    private Set<OWLEquivalentClassesAxiom> equivalences;
    private Set<OWLDisjointUnionAxiom> disjointUnion;
    private Set<OWLDisjointClassesAxiom> disjointClasses;

    public ClassFrame(OWLClassExpression expr){
        this.entity = expr; 
        this.superclasses = new HashSet<>();
        this.equivalences = new HashSet<>();
        this.disjointUnion = new HashSet<>();
        this.disjointClasses = new HashSet<>();
    }

    public OWLClassExpression getClassExpression(){
        return this.entity;
    }

    public int getSize(){
        return this.superclasses.size() +
            this.equivalences.size() +
            this.disjointUnion.size() + 
            this.disjointClasses.size();
    }

    public Set<OWLSubClassOfAxiom> getSuperClasses(){
        return this.superclasses;
    }
    public Set<OWLEquivalentClassesAxiom> getEquivalentClasses(){
        return this.equivalences;
    }
    public Set<OWLDisjointUnionAxiom> getDisjointUnion(){
        return this.disjointUnion;
    }
    public Set<OWLDisjointClassesAxiom> getDisjointClasses(){
        return this.disjointClasses;
    } 

    public void addSuperclass(OWLSubClassOfAxiom c){
        this.superclasses.add(c);
    } 
    public void addSuperclasses(Set<OWLSubClassOfAxiom> cs){
        this.superclasses.addAll(cs);
    } 
    public void addEquivalence(OWLEquivalentClassesAxiom eqs){
        this.equivalences.add(eqs);
    }
    public void addEquivalences(Set<OWLEquivalentClassesAxiom> eqs){
        this.equivalences.addAll(eqs);
    }
    public void addDisjointUnion(OWLDisjointUnionAxiom du){
        this.disjointUnion.add(du);
    }
    public void addDisjointUnions(Set<OWLDisjointUnionAxiom> du){
        this.disjointUnion.addAll(du);
    }
    public void addDisjointClass(OWLDisjointClassesAxiom c){
        this.disjointClasses.add(c);
    } 
    public void addDisjointClasses(Set<OWLDisjointClassesAxiom> c){
        this.disjointClasses.addAll(c);
    } 
}

