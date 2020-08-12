package uk.ac.man.cs.exp.axioms.coverage;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.regularities.axiom.*;
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
        AxiomGroundGeneralisation coverage = new AxiomGroundGeneralisation(ont,0.9);
        log.info(timer.stop("GroundGeneralisation for "  + ontologyName));

        TreeMap<Integer,Set<SyntaxTree>> kCoverage = coverage.getKCoverage();
        //format : ontology name, k, r_1,r_2...,r_k
        int classAxiomSize = coverage.getClassAxiomSize();
        String groundGeneralisationFolder = output + "/groundGeneralisationCoverage";
        IOHelper.createFolder(groundGeneralisationFolder);

        String formatRenaming = ontologyName + "," + kCoverage.size() + ","; 
        for(Map.Entry<Integer,Set<SyntaxTree>> entry : kCoverage.entrySet()){
            int size = entry.getKey();
            Set<SyntaxTree> regularities = entry.getValue(); 
            for(SyntaxTree r : regularities){
                double prominence = size / (double) classAxiomSize;
                formatRenaming += prominence + ",";
                //Record: prominence + axiom
                IOHelper.writeAppend(prominence + "," + r.getRoot().toString(),
                        groundGeneralisationFolder + "/" + ontologyName); 
            }
        } 
        //summary file
        IOHelper.writeAppend(formatRenaming, output + "/groundGeneralisation");

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

    public int getClassAxiomSize(){
        return this.classAxioms;
    }

    public TreeMap<Integer,Set<SyntaxTree>> getKCoverage(){

        Map<SyntaxTree,Set<OWLAxiom>> reg2instance = this.miner.getRegularity2instance();
        //order regularities according to size (largest first)
        TreeMap<Integer,Set<SyntaxTree>> size2regularity = new TreeMap(Collections.reverseOrder()); 
        for(SyntaxTree reg : reg2instance.keySet()){
            Set<OWLAxiom> instances = reg2instance.get(reg);
            int size = instances.size();
            size2regularity.putIfAbsent(size,new HashSet<>());
            size2regularity.get(size).add(reg);
        } 

        //iterate according to regularities' size
        //until threshold is reached
        //record involved regularities
        TreeMap<Integer,Set<SyntaxTree>> kRegularity = new TreeMap(Collections.reverseOrder()); 
        Set<OWLAxiom> coverage = new HashSet<>();//this will be necessary for 'sets of axioms'
        for(Map.Entry<Integer,Set<SyntaxTree>> entry : size2regularity.entrySet()){
            Integer size = entry.getKey();
            Set<SyntaxTree> nodes2insert = entry.getValue();

            for(SyntaxTree t : nodes2insert){

                coverage.addAll(reg2instance.get(t));
                kRegularity.putIfAbsent(size,new HashSet<>());
                kRegularity.get(size).add(t);

                double test = coverage.size() / (double) this.classAxioms;
                if(test >= this.threshold){
                    return kRegularity;
                }
            } 
        } 
        return kRegularity;
    } 
}
