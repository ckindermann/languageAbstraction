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

    private boolean noLogicalAxioms;
    private boolean noLogicalAxiomsFiltered;
    private boolean isHierarchy;
    private boolean isEL;
    private boolean isNotEL; 

    public OntologyProfiler(String ontPath, boolean importsIncluded) throws Exception {
        this.ontologyPath = ontPath; 

        this.manager = OWLManager.createOWLOntologyManager(); 
        OntologyLoader loader = new OntologyLoader(new File(ontPath), importsIncluded);
        this.ontology = loader.getOntology();

        noLogicalAxioms = false;
        noLogicalAxiomsFiltered = false;
        isHierarchy = false;
        isEL = false;
        isNotEL = false;

        this.initialise();
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
        this.determineClassHierarchy();
        this.determineExpressivity(); 
        this.determineEmptinessFiltered();
    }

    private void determineEmptinessFiltered() throws Exception {
        Set<OWLLogicalAxiom> ontAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);
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

        Set<OWLLogicalAxiom> ontAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);
        if(ontAxioms.isEmpty())
            this.noLogicalAxioms = true;
        else
            this.noLogicalAxioms = false; 
    }


    private void determineClassHierarchy() throws Exception {

        //yes if all axioms in ontology are atomic subsumptions or atomic equvialence axioms
        ExplicitClassHierarchy ech = new ExplicitClassHierarchy(this.ontology);

        Set<OWLLogicalAxiom> ontAxioms = this.ontology.getLogicalAxioms(Imports.INCLUDED);
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

