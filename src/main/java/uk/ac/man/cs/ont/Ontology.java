package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import org.semanticweb.owlapi.search.EntitySearcher;

import java.io.File;
import java.util.*;
import java.util.stream.*;

/**
 * Created by chris on 12/09/17.
 */
public class Ontology {

    private OWLOntology ontology;
    private Set<String> imports;
    private Set<OWLEntity> signature;

    public Ontology (OWLOntology o) {
        this.ontology = o;
        this.signature = this.ontology.getSignature();
    }

    public Ontology (File ontFile, boolean imports){
        OntologyLoader loader = new OntologyLoader(ontFile, imports);
        this.ontology = loader.getOntology();
        this.signature = this.ontology.getSignature(); 
    }

    public OWLOntology getOntology() {
        return ontology;
    }

    public Set<OWLEntity> getSignature(){
        return this.signature;
    }

    private boolean checkEntity(OWLEntity entity){
        return (this.ontology.getSignature().contains(entity));
    }

    public Set<String> getImportClosure(){
        if(this.imports.isEmpty())
            this.computeImportClosure();
        return this.imports;
    }
    
    private void computeImportClosure(){
        Set<OWLOntology> importClosure = this.ontology.getImportsClosure();
        for(OWLOntology o : importClosure){
            if(o.getOntologyID().getOntologyIRI().isPresent()){ 
                this.imports.add(o.getOntologyID().getOntologyIRI().get().toString()); 
            }
        }
    } 
}
