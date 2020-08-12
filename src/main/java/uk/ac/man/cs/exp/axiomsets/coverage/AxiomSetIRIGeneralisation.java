package uk.ac.man.cs.exp.axiomsets.coverage;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.regularities.axiomsets.irig.*; 
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
public class AxiomSetIRIGeneralisation {

    private static final Logger log = Logger.getLogger(String.valueOf(AxiomSetIRIGeneralisation.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String output = args[1]; 

        File ontFile = new File(ontFilePath);
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        MyTimer timer = new MyTimer();
        timer.go();
        AxiomSetIRIGeneralisation coverage = new AxiomSetIRIGeneralisation(ont,0.9);
        log.info(timer.stop("IRIGeneralisation for "  + ontologyName));

        TreeMap<Integer,Set<ClassFrameIRIGeneralisation>> kCoverage = coverage.getKCoverage();

        int classAxiomSize = coverage.getClassAxiomSize();
        String iriGeneralisationFolder = output + "/IRIGeneralisationCoverage";
        //IOHelper.createFolder(iriGeneralisationFolder);

        String format = ontologyName + "," + kCoverage.size() + ","; 
        for(Map.Entry<Integer,Set<ClassFrameIRIGeneralisation>> entry : kCoverage.entrySet()){
            int size = entry.getKey();
            Set<ClassFrameIRIGeneralisation> regularities = entry.getValue(); 
            for(ClassFrameIRIGeneralisation r : regularities){
                double prominence = coverage.getCoveredAxioms(r).size() / (double) classAxiomSize;
                format += prominence + ",";
                //IOHelper.writeAppend(prominence + "," + r.toString().replaceAll("\n","#"),
                //        iriGeneralisationFolder + "/" + ontologyName); 
            } 
        }
        IOHelper.writeAppend(format, output + "/IRIGeneralisation");
    }

    private OWLOntology ontology;
    private ClassFrameIRIGMiner miner;
    private Map<OWLClassExpression,ClassFrame> class2frame;

    private double threshold;

    private int logicalAxioms;
    private int classAxioms;

    public AxiomSetIRIGeneralisation(OWLOntology o, double t){
        this.setOntology(o);
        this.miner = new ClassFrameIRIGMiner(o); 
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

    public int getClassAxiomSize(){
        return this.classAxioms;
    }

    public TreeMap<Integer,Set<ClassFrameIRIGeneralisation>> getKCoverage(){
        Map<ClassFrameIRIGeneralisation,Set<ClassFrameIRIGeneralisation>>
            reg2instance = this.miner.getRegularity2instance();
        TreeMap<Integer,Set<ClassFrameIRIGeneralisation>> size2regularity = new TreeMap(Collections.reverseOrder());
        for(ClassFrameIRIGeneralisation reg : reg2instance.keySet()){
            Set<ClassFrameIRIGeneralisation> instances = reg2instance.get(reg);
            int size = getClassFrameAxioms(instances).size();
            size2regularity.putIfAbsent(size,new HashSet<>());
            size2regularity.get(size).add(reg); 
        }

        TreeMap<Integer,Set<ClassFrameIRIGeneralisation>> kRegularity = new TreeMap(Collections.reverseOrder());
        Set<OWLAxiom> coverage = new HashSet<>();
        for(Map.Entry<Integer,Set<ClassFrameIRIGeneralisation>> entry : size2regularity.entrySet()){
            Integer size = entry.getKey();
            Set<ClassFrameIRIGeneralisation> nodes2insert = entry.getValue();

            for(ClassFrameIRIGeneralisation f : nodes2insert){
                coverage.addAll(getClassFrameAxioms(reg2instance.get(f)));
                kRegularity.putIfAbsent(size,new HashSet<>());
                kRegularity.get(size).add(f);

                double test = coverage.size() / (double) this.classAxioms;
                if(test >= this.threshold){
                    return kRegularity;
                }
            } 
        }

        return kRegularity;
    }

    public Set<OWLAxiom> getCoveredAxioms(ClassFrameIRIGeneralisation regularity){
        //Set<OWLAxiom> res = new HashSet<>();
        Map<ClassFrameIRIGeneralisation,Set<ClassFrameIRIGeneralisation>>
            reg2instance = this.miner.getRegularity2instance();
        return getClassFrameAxioms(reg2instance.get(regularity));

    }

    private Set<OWLAxiom> getClassFrameAxioms(Set<ClassFrameIRIGeneralisation> frames){
        Set<OWLAxiom> axioms = new HashSet<>();
        for(ClassFrameIRIGeneralisation frame : frames){
            ClassFrame f = this.class2frame.get(frame.getClassExpression());
            axioms.addAll(f.getAxioms()); 
        } 
        return axioms;
    } 
}
