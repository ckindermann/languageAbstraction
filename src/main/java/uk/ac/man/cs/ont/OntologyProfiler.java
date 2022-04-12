package uk.ac.man.cs.ont;

import uk.ac.man.cs.util.*;

import org.semanticweb.owlapi.model.OWLOntology; 
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.*;
import java.io.FileWriter;
import java.util.*;
import java.io.File;

/**
 * Created by chris on 15/09/17.
 *
 * check whether ontology is (logically) empty
 * check expressivity (classhierarchy, EL++, other)
 *
 */
public class OntologyProfiler {

    private String ontologyPath;
    //private File ontologyFile;

    private OWLOntology ontology;
    private OWLOntologyManager manager; 
    private boolean importsIncluded;

    private boolean noLogicalAxioms;
    private boolean noLogicalAxiomsFiltered;
    private boolean noClassExpressionAxioms;
    private boolean isHierarchy;
    private boolean isEL;
    private boolean isNotEL; 

    public OntologyProfiler(String ontPath, boolean importsIncluded) throws Exception {
        this.ontologyPath = ontPath; 

        this.manager = OWLManager.createOWLOntologyManager(); 
        OntologyLoader loader = new OntologyLoader(new File(ontPath), importsIncluded);
        this.ontology = loader.getOntology();

        this.importsIncluded=importsIncluded;

        noLogicalAxioms = false;
        noLogicalAxiomsFiltered = false;
        noClassExpressionAxioms = false;
        isHierarchy = false;
        isEL = false;
        isNotEL = false;

        this.initialise();
    } 

    public boolean hasNoClassExpressionAxioms(){
        return this.noClassExpressionAxioms;
    }

    public boolean isLogicallyEmpty(){
        return this.noLogicalAxioms;
    }

    public boolean isLogicallyEmptyFiltered(){
        return this.noLogicalAxiomsFiltered; 
    } 

    public boolean isHierarchy(){ 
        return this.isHierarchy;
    }

    public boolean isEL(){
        return this.isEL; 
    }

    public boolean isNotEL(){
        return this.isNotEL; 
    }

    private void initialise() throws Exception {
        this.determineEmptiness();
        this.determineClassExpressionAxioms();
        this.determineClassHierarchy();
        this.determineExpressivity(); 
        this.determineEmptinessFiltered();
    }

    private void determineClassExpressionAxioms() throws Exception {

        Set<OWLAxiom> ontAxioms = new HashSet<>();
        if(this.importsIncluded){
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED));
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED));
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.DISJOINT_UNION, Imports.INCLUDED));
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.INCLUDED)); 
        } else {
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.SUBCLASS_OF, Imports.EXCLUDED));
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.EXCLUDED));
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.DISJOINT_UNION, Imports.EXCLUDED));
            ontAxioms.addAll(this.ontology.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.EXCLUDED)); 
        }

        if(ontAxioms.isEmpty())
            this.noClassExpressionAxioms = true;
        else
            this.noClassExpressionAxioms = false; 
    }

    private void determineEmptinessFiltered() throws Exception {

        Set<OWLLogicalAxiom> ontAxioms;
        if(this.importsIncluded){
            ontAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);
        } else {
            ontAxioms = this.ontology.getLogicalAxioms(Imports.EXCLUDED);
        }

        Set<OWLAxiom> convert = new HashSet<>();
        for(OWLLogicalAxiom a : ontAxioms){
            convert.add((OWLAxiom) a); 
        }
        AxiomFilter filter = new AxiomFilter(convert);
        filter.removeTautologies();
        if(filter.getAxioms().isEmpty())
            this.noLogicalAxiomsFiltered = true;
        else
            this.noLogicalAxiomsFiltered = false; 
    }

    private void determineEmptiness() throws Exception {

        Set<OWLLogicalAxiom> ontAxioms;
        if(this.importsIncluded){
            ontAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);
        } else {
            ontAxioms = this.ontology.getLogicalAxioms(Imports.EXCLUDED);
        } 

        if(ontAxioms.isEmpty())
            this.noLogicalAxioms = true;
        else
            this.noLogicalAxioms = false; 
    }


    private void determineClassHierarchy() throws Exception {

        //yes if all axioms in ontology are atomic subsumptions or atomic equvialence axioms
        ExplicitClassHierarchy ech = new ExplicitClassHierarchy(this.ontology);

        Set<OWLLogicalAxiom> ontAxioms;
        if(this.importsIncluded){
            ontAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);
        } else {
            ontAxioms = this.ontology.getLogicalAxioms(Imports.EXCLUDED);
        } 

        Set<OWLAxiom> echAxioms = ech.getAxioms();

        for(OWLLogicalAxiom a : ontAxioms){
            if(!echAxioms.contains((OWLAxiom) a)){
                this.isHierarchy = false;
                return;
            } 
        }
        this.isHierarchy = true;
    } 

    private void determineExpressivity() throws Exception { 
        Set<OWLOntology> ontSet = new HashSet<>();
        ontSet.add(this.ontology);
        DLExpressivityChecker checker = new DLExpressivityChecker(ontSet);

        if(checker.isWithin(Languages.ELPLUSPLUS))
            this.isEL = true;
        else
            this.isNotEL = true;
    }
}

