package uk.ac.man.cs.exp.constructors.axioms;


import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.regularities.axiom.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.iso.gg.*;
import uk.ac.man.cs.hierarchy.axioms.gg.*;

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
public class AxiomGGSize {

    private static final Logger log = Logger.getLogger(String.valueOf(AxiomGGSize.class));

    public static void main(String[] args) throws IOException , Exception {

        String ontFilePath = args[0]; 
        String outputPath = args[1]; 

        File ontFile = new File(ontFilePath);
        //log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();
        IOHelper.createFolder(outputPath + "/" + ontologyName);


        SyntaxTreeBuilder treeBuilder = new SyntaxTreeBuilder();

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

        AxiomRegularityHierarchyImp hImp = new AxiomRegularityHierarchyImp(syntrees); 

        //ontology for example
        //ontology for all instances
        //file for example,depth,size,max branch,leafs,non-leafs,average branching 

        hImp.writeGraphWithInstances(outputPath + "/" + ontologyName);

        Set<HierarchyNode> nodes = hImp.getNodes();

        writeInstances(nodes, outputPath + "/" + ontologyName);
        writeStatistics(nodes, outputPath + "/" + ontologyName);

    }

    public static void writeInstances(Set<HierarchyNode> nodes, String output) throws Exception {
        String basePath = output + "/instances";
        IOHelper.createFolder(basePath);
        for(HierarchyNode node : nodes){
            Set<OWLAxiom> instances = node.getInstances();
            OntologySaver.saveAxioms(instances, basePath + "/" + node.getID() + ".owl"); 
        }
    }

    public static void writeStatistics(Set<HierarchyNode> nodes, String output) throws Exception {
        //regularity size (number of instances)
        //regularity structure size (number of nodes)
        //number of leafs
        //number of non-leafs
        //max branch
        //average branching
        String basePath = output + "/statistics";
        for(HierarchyNode node : nodes){
            SyntaxTree synTree = node.getTree();
            int regularitySize = node.getInstances().size();
            int structureSize = synTree.getTree().vertexSet().size();
            int depth = getDepth(synTree);
            int leafs = getLeafs(synTree);
            int nonLeafs = structureSize - leafs;
            int maxBranching = getMaxmialBranchingFactor(synTree);
            double averageBranching = ((double) structureSize  - 1) / nonLeafs;

            String sum = node.getID() + ":" +
                regularitySize + "," +
                structureSize + "," +
                depth + "," +
                leafs + "," +
                nonLeafs + "," +
                maxBranching + "," +
                averageBranching;


            IOHelper.writeAppend(sum, basePath);

        } 
    }

    public static int getDepth(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> tree = t.getTree();
        int depth = -1;
        SyntaxNode root = t.getRoot();
        Set<SyntaxNode> level = new HashSet<>(); 
        level.add(root);
        Set<SyntaxNode> nextLevel = new HashSet<>();

        while(!level.isEmpty()){
            depth++;
            for(SyntaxNode n : level){ 
                //construct next level
                Set<DefaultEdge> edges = tree.outgoingEdgesOf(n); 
                for(DefaultEdge e : edges){
                    nextLevel.add(tree.getEdgeTarget(e)); 
                }
            }
            level.clear();
            level.addAll(nextLevel);
            nextLevel.clear();
        } 
        return depth;
    }

    public static int getLeafs(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> tree = t.getTree();
        int leafs = 0;
        for (SyntaxNode n : tree.vertexSet()) { 
            if(tree.outgoingEdgesOf(n).size() == 0){
                leafs++;
            }
        } 
        return leafs;
    }
    //public static int getNonLeafs(SyntaxTree t){ return 0; }
    public static int getMaxmialBranchingFactor(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> tree = t.getTree();
        int maxBranching = 0;
        for (SyntaxNode n : tree.vertexSet()) { 
            int branching = tree.outgoingEdgesOf(n).size();
            if(branching > maxBranching){
                maxBranching = branching;
            }
        } 
        return maxBranching; 
    } 

}
