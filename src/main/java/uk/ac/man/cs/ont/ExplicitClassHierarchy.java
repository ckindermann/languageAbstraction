package uk.ac.man.cs.ont;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;

import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import java.util.*;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import java.io.FileWriter;
import org.semanticweb.owlapi.model.parameters.*;
import java.io.File;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports; 


/**
 * Created by slava on 15/09/17.
 */
public class ExplicitClassHierarchy {

    private OWLOntology ontology;
    private OWLOntology materialisation;

    private Set<OWLSubClassOfAxiom> atomicSubsumptions;
    private Set<OWLEquivalentClassesAxiom> equivalenceAxioms; 

    private OWLOntologyManager manager;

    public ExplicitClassHierarchy(OWLOntology o) throws Exception {

        this.manager = OWLManager.createOWLOntologyManager(); 
        this.ontology = o;

        //initialise asserted class hierarchy
        this.materialisation = this.manager.createOntology(IRI.generateDocumentIRI());

        this.atomicSubsumptions = new HashSet<>();
        this.equivalenceAxioms = new HashSet<>();

        this.compile(); 
    }

    public Set<OWLAxiom> getAxioms() {
        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.addAll(this.atomicSubsumptions);
        axioms.addAll(this.equivalenceAxioms);
        return axioms;
    }

    public Set<OWLSubClassOfAxiom> getAtomicSubsumptions() {
        return this.atomicSubsumptions; 
    }

    public Set<OWLEquivalentClassesAxiom> getEquivalenceAxioms(){
        return this.equivalenceAxioms; 
    }

    private boolean compile() throws Exception {
        Set<OWLAxiom> axioms = this.ontology.getAxioms(Imports.INCLUDED); 

        for(OWLAxiom a : axioms){
            if(isAtomicSubsumption(a)){
                this.atomicSubsumptions.add((OWLSubClassOfAxiom) a);
            }
            if(isAtomicEquivalence(a)){
                this.equivalenceAxioms.add((OWLEquivalentClassesAxiom) a); 
            }
        }

        //what happens if equivalenceAxioms are empty?
        ChangeApplied c1 = this.manager.addAxioms(this.materialisation, atomicSubsumptions);
        ChangeApplied c2 = this.manager.addAxioms(this.materialisation, equivalenceAxioms); 
        if(c1.equals(ChangeApplied.SUCCESSFULLY) && c2.equals(ChangeApplied.SUCCESSFULLY))
            return true;
        if(c1.equals(ChangeApplied.SUCCESSFULLY) && equivalenceAxioms.isEmpty())
            return true;
        if(atomicSubsumptions.isEmpty() && c2.equals(ChangeApplied.SUCCESSFULLY))
            return true;
        if(atomicSubsumptions.isEmpty() && equivalenceAxioms.isEmpty())
            return true;
        return false;
    } 

    public OWLOntology getAsOntology() throws Exception {
        return this.materialisation;
    }

    private boolean isAtomicEquivalence(OWLAxiom a){
        if(a instanceof OWLEquivalentClassesAxiom){
            Set<OWLClassExpression> topLevelClasses = ((OWLEquivalentClassesAxiom) a).getClassExpressions(); //toplevel classes of the axiom
            Set<OWLClass> namedClassesHelper = ((OWLEquivalentClassesAxiom) a).getNamedClasses(); // all named classes in this equivalence axiom
            Set<OWLClassExpression> namedClasses = new HashSet<>();
            namedClasses.addAll(namedClassesHelper);

            for(OWLClassExpression c : topLevelClasses){
                if(!namedClasses.contains(c))
                    return false; 
            }

            return true;
        }
        return false;
    }

    private boolean isAtomicSubsumption(OWLAxiom a){
        if(a instanceof OWLSubClassOfAxiom){
            OWLClassExpression subclass = ((OWLSubClassOfAxiom) a).getSubClass();
            OWLClassExpression superclass = ((OWLSubClassOfAxiom) a).getSuperClass();
            if(!subclass.isAnonymous() && !superclass.isAnonymous())
                return true; 
        }
        return false;
    } 
}
