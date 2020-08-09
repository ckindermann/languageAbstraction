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

public class OntologyReasonerChecker {

    private String ontologyPath;
    private File ontologyFile;
    private String outputPath;
    private String reasonerName;

    private OWLOntology ontology;
    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    private boolean loadable; 
    private boolean classifiable; 

    public OntologyReasonerChecker(String ontPath, String rName, String outPath){
        this.ontologyPath = ontPath;
        this.outputPath = outPath; 
        this.reasonerName = rName;
        this.manager = OWLManager.createOWLOntologyManager();
        this.ontologyFile = new File(this.ontologyPath);
    }

    public void writeReport(){
        String target = outputPath + "/" + this.reasonerName;
        IOHelper.createFolder(target);
        if(this.loadable){
            IOHelper.writeAppend(this.ontologyFile.getName(), target + "/loadable"); 
            if(this.classifiable){
                IOHelper.writeAppend(this.ontologyFile.getName(), target + "/classifiable"); 
            }
        }
    }

    public void check(){
        this.loadable = isLoadable(this.ontologyFile);
        if(this.loadable){
            this.classifiable = isClassifiable(this.ontology); 
        }
    }

    public boolean isLoadable(File ontologyFile){ 
        try {
            this.ontology = this.manager.loadOntologyFromOntologyDocument(ontologyFile); 
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isClassifiable(OWLOntology o){
        try {
            reasoner = ReasonerLoader.initReasoner(ReasonerName.get(this.reasonerName), o);
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        } catch (Exception e) {
            return false; 
        }
        return true; 
    }
}
