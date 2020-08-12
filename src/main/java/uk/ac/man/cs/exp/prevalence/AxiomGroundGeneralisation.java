package uk.ac.man.cs.exp.prevalence;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.regularities.axiom.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.iso.gg.*;

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

import java.nio.file.*;

import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

/**
 * A class to demonstrate the functionality of the library.
 */
public class AxiomGroundGeneralisation {

    private static final Logger log = Logger.getLogger(String.valueOf(AxiomGroundGeneralisation.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String output = args[1]; 

        File ontFile = new File(ontFilePath);
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        MyTimer timer = new MyTimer();
        timer.go();
        AxiomGroundGeneralisation prevalence = new AxiomGroundGeneralisation(ont,0.1);
        log.info(timer.stop("GroundGeneralisation for "  + ontologyName));

        Map<SyntaxTree,Set<OWLAxiom>> prevalentIRIgeneralisations = prevalence.getPrevalentRegularities();
        int prevalent = prevalentIRIgeneralisations.size();

        IOHelper.writeAppend(ontologyName + "," + prevalent, output + "/groundGeneralisation"); 
    }

    private OWLOntology ontology;
    private GroundGeneralisationMiner miner;

    private double threshold;

    private int logicalAxioms;
    private int classAxioms;

    public AxiomGroundGeneralisation(OWLOntology o, double t){
        this.setOntology(o);
        this.miner = new GroundGeneralisationMiner(o);
        this.threshold = t; 
    } 

    public void setOntology(OWLOntology o){
        this.ontology = o;
        this.computeSizes();
    }

    private void computeSizes(){
        this.classAxioms = 0;
        this.classAxioms += this.ontology.getAxioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED).size();
        this.classAxioms += this.ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED).size();
        this.classAxioms += this.ontology.getAxioms(AxiomType.DISJOINT_UNION, Imports.INCLUDED).size();
        this.classAxioms += this.ontology.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.INCLUDED).size();
        this.logicalAxioms = this.ontology.getLogicalAxioms(true).size(); 
    } 

    public Map<SyntaxTree,Set<OWLAxiom>> getPrevalentRegularities(){
        Map<SyntaxTree,Set<OWLAxiom>> prevalent = new HashMap<>();
        Map<SyntaxTree,Set<OWLAxiom>> reg2instance = this.miner.getRegularity2instance();
        for(SyntaxTree reg : reg2instance.keySet()){
            Set<OWLAxiom> instances = reg2instance.get(reg);
            double test = instances.size() / (double) this.classAxioms;
            if(test >= this.threshold){
                prevalent.put(reg,instances);
            } 
        } 
        return prevalent;
    }
}
