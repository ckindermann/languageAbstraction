package uk.ac.man.cs.exp.axiomsets.prevalence;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.regularities.axiomsets.cep.*; 
import uk.ac.man.cs.regularities.axiomsets.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.iso.irig.*;

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
public class AxiomSetCEPreservation {

    private static final Logger log = Logger.getLogger(String.valueOf(AxiomSetCEPreservation.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String output = args[1]; 

        File ontFile = new File(ontFilePath);
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        MyTimer timer = new MyTimer();
        timer.go();
        AxiomSetCEPreservation prevalence = new AxiomSetCEPreservation(ont,0.1);
        log.info(timer.stop("CEP for "  + ontologyName));

        //TODO:
        //1. get map from class entities to class frame (vanilla classframe miner)
        //2. get map from classFrameIRIgeneralisaiton to entiy (via regualrity miner)
        //3. measure prevalence
        Map<ClassFrameCEPreservation,Set<ClassFrameCEPreservation>>
            prevalentCEPreservations = prevalence.getPrevalentRegularities();
        int prevalent = prevalentCEPreservations.size();

        IOHelper.writeAppend(ontologyName + "," + prevalent, output + "/classExpressionPreservance"); 
    }

    private OWLOntology ontology;
    private ClassFrameCEPMiner miner;
    private Map<OWLClassExpression,ClassFrame> class2frame;

    private double threshold;

    private int logicalAxioms;
    private int classAxioms;

    public AxiomSetCEPreservation(OWLOntology o, double t){
        this.setOntology(o);
        this.miner = new ClassFrameCEPMiner(o); 
        this.class2frame = this.miner.getClass2Frame();
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

    public Map<ClassFrameCEPreservation,Set<ClassFrameCEPreservation>>
        getPrevalentRegularities(){

        Map<ClassFrameCEPreservation,Set<ClassFrameCEPreservation>> prevalent
            = new HashMap<>(); 

        Map<ClassFrameCEPreservation,Set<ClassFrameCEPreservation>>
            reg2instance = this.miner.getRegularity2instance();

        for(ClassFrameCEPreservation reg : reg2instance.keySet()){
            Set<ClassFrameCEPreservation> instances = reg2instance.get(reg);
            Set<OWLAxiom> coveredAxioms = this.getClassFrameAxioms(instances); 
            double test = coveredAxioms.size() / (double) this.classAxioms;
            if(test >= this.threshold){
                prevalent.put(reg,instances);
            } 
        } 
        return prevalent;
    }

    private Set<OWLAxiom> getClassFrameAxioms(Set<ClassFrameCEPreservation> frames){
        Set<OWLAxiom> axioms = new HashSet<>();
        for(ClassFrameCEPreservation frame : frames){
            ClassFrame f = this.class2frame.get(frame.getClassExpression());
            axioms.addAll(f.getAxioms()); 
        } 
        return axioms;
    } 
}
