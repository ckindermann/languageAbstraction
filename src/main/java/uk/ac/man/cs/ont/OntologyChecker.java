package uk.ac.man.cs.ont;

import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.ont.*;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import org.semanticweb.owlapi.model.IRI;
import java.io.IOException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.util.AutoIRIMapper;


/**
 * Created by chris on 16/08/19.
 *
 * Tests whether a given ontology can be
 * (a) loaded
 * (b) classified
 * (c) converted to RDF/XML *
 */

public class OntologyChecker {

    private String ontologyPath;
    private File ontologyFile;
    private String outputPath;

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    private boolean loadable; 
    private boolean classifiable; 
    private boolean convertible; 

    public OntologyChecker(String ontPath, String outPath){
        this.ontologyPath = ontPath;
        this.outputPath = outPath; 
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontologyFile = new File(this.ontologyPath);
    }

    public void writeReport(){
        if(this.loadable){
            IOHelper.writeAppend(this.ontologyFile.getName(), outputPath + "/loadable"); 
            if(this.classifiable){
                IOHelper.writeAppend(this.ontologyFile.getName(), outputPath + "/classifiable"); 
            }
            if(this.convertible){
                IOHelper.writeAppend(this.ontologyFile.getName(), outputPath + "/convertible"); 
            }
        } 
    }

    public void check(){
        this.loadable = isLoadable();
        if(this.loadable){
            this.classifiable = isClassifiable(this.ontology); 
            //this.convertible = isConvertible(this.ontology);
        }
    }

    public boolean isLoadable(){ 
        try {
            this.ontology = this.manager.loadOntologyFromOntologyDocument(this.ontologyFile); 
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } 
        return true;
    }

    public boolean isClassifiable(OWLOntology o){
        try {
            reasoner = ReasonerLoader.initReasoner(o);
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        } catch (Exception e) {
            return false; 
        }
        return true; 
    }

    public boolean isConvertible(){ 
        IOHelper.createFolder(outputPath+"/RDFXML"); 
        String file = this.outputPath + "/RDFXML/" + this.ontologyFile.getName();
        File output = new File(file);
        IRI documentIRI2 = IRI.create(output.toURI());

        try{
            manager.saveOntology(this.ontology, new RDFXMLDocumentFormat(), documentIRI2); 
        } catch (Exception e){
            return false; 
        }
        return true;
    }
}
