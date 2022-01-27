package uk.ac.man.cs.hierarchy.axioms.gg;


import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.pat.gg.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.subIso.*;

import uk.ac.man.cs.regularities.axiom.*;

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
public class TestHierarchy {

    private static final Logger log = Logger.getLogger(String.valueOf(TestHierarchy.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String abstraction = args[1];  // internal or gg
        String outputPath = args[2]; 

        //get ontFileName
        String ontologyName = Paths.get(ontFilePath).getFileName().toString();
        //create folder
        outputPath = outputPath + "/" + ontologyName;
        IOHelper.createFolder(outputPath);

        //quick hack to convert ontology from TTL to owlxml
        //log.info("\tChecking Ontology "); 
        //OntologyChecker checker = new OntologyChecker(ontFilePath, outputPath);
        //checker.isLoadable(new File(ontFilePath));
        //checker.isConvertible();
        //log.info("\tDone Checking"); 


        //Load ontology
        File ontFile = new File(ontFilePath);
        log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 


        //SyntaxTreeBuilder treeBuilder = new SyntaxTreeBuilder();
        //ConstructorPreservanceBuilder treeBuilder = new ConstructorPreservanceBuilder(); 

        TreeBuilder treeBuilder;

        switch (abstraction) {
            case "gg":
            treeBuilder = new SyntaxTreeBuilder();
            break;

            case "internal":
            treeBuilder = new ConstructorPreservanceBuilder(); 
            break;

            default:
            treeBuilder = new SyntaxTreeBuilder();
        }

        //Get Class Axioms
        Set<OWLAxiom> toTest = new HashSet<>();
        toTest.addAll(ont.getAxioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED));
        toTest.addAll(ont.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED));
        toTest.addAll(ont.getAxioms(AxiomType.DISJOINT_UNION, Imports.INCLUDED));
        toTest.addAll(ont.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.INCLUDED));

        //Build syntax trees
        Set<SyntaxTree> syntrees = new HashSet<>();
        for(OWLAxiom a : toTest){
            syntrees.add(treeBuilder.build(a));
        } 
        System.out.println("Number of axioms : " + toTest.size());

        MyTimer timer = new MyTimer();

        //get regularities over axioms
        //timer.go();
        //GroundGeneralisationMiner gg = new GroundGeneralisationMiner(ont);
        //timer.stop("GG"); 
        //Map<SyntaxTree,Set<OWLAxiom>> reg2instances = gg.getRegularity2instance();
        //System.out.println("GG" + reg2instances.size());

        //Set<SyntaxTree> regularities = reg2instances.keySet(); 
        //
        timer.go(); 
        AxiomRegularityHierarchyImp hImp = new AxiomRegularityHierarchyImp(syntrees); 
        timer.stop("impHG");

        System.out.println("nodes: " + hImp.getNodes().size());
        System.out.println("roots: " + hImp.getRoots().size()); 

        Set<HierarchyNode> nodes = hImp.getNodes();
        Set<HierarchyNode> roots = hImp.getRoots();

        int maxDepth=0;
        int maxBranching=0;
        for(HierarchyNode n : nodes){
            if(n.getDepth() > maxDepth){
                maxDepth = n.getDepth();
            }
            if(n.getChildren().size() > maxBranching){
                maxBranching = n.getChildren().size();
            } 
        }

        System.out.println("Max Depth: " + maxDepth); 
        System.out.println("Max Branching: " + maxBranching); 

        Set<HierarchyNode> leafs = new HashSet();
        for(HierarchyNode n : nodes){
            if(n.getChildren().isEmpty()){
                leafs.add(n);
            }
        }


        double branchingFactor =  ((double) (nodes.size() - roots.size())) / (nodes.size() - leafs.size());
        System.out.println("Average Branching: " + branchingFactor); 

        //list of nodes + number of instances
        for(HierarchyNode n : nodes){
            System.out.println(n.getID() + " " + n.getInstances().size()); 
        }


        hImp.writeGraphWithInstances(outputPath);

        //print stuff
        //for(HierarchyNode n : hImp.getNodes()){
        //    OWLAxiom a = ((AxiomNode) n.getTree().getRoot()).getAxiom();
        //    System.out.println("Node " + n.getID() + " " + a.toString());
        //    for(HierarchyNode c : n.getChildren()){
        //        OWLAxiom b = ((AxiomNode) c.getTree().getRoot()).getAxiom(); 
        //        System.out.println(a.toString() + " -> " + b.toString());
        //    } 
        //    System.out.println("----");
        //}

        //write nodes as ontologies
        for(HierarchyNode n : nodes){
            int ID = n.getID();
            Set<OWLAxiom> instances = n.getInstances();

            OntologySaver.saveAxioms(instances, outputPath + "/ontologies/" + ID);
        }
    }
}
