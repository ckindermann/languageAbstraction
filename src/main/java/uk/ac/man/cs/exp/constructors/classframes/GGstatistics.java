package uk.ac.man.cs.exp.constructors.classframes;


import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.regularities.axiom.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.iso.gg.*;
//import uk.ac.man.cs.hierarchy.axioms.gg.*;
import uk.ac.man.cs.hierarchy.axiomsets.gg.*;
import uk.ac.man.cs.regularities.axiomsets.gg.*;
import uk.ac.man.cs.regularities.axiomsets.*;

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
public class GGstatistics {

    private static final Logger log = Logger.getLogger(String.valueOf(GGstatistics.class));

    public static void main(String[] args) throws IOException , Exception {

        String ontFilePath = args[0]; 
        String outputPath = args[1]; 

        File ontFile = new File(ontFilePath);
        //log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();
        IOHelper.createFolder(outputPath + "/" + ontologyName);

        ClassFrameGGMiner frameMiner = new ClassFrameGGMiner(ont);
        Map<ClassFrameGroundGeneralisation,Set<ClassFrameGroundGeneralisation>> reg2instH = frameMiner.getRegularity2instance();


        //conversion for instances
        Map<ClassFrameGroundGeneralisation,Set<OWLClassExpression>> reg2inst = new HashMap<>();
        for (Map.Entry<ClassFrameGroundGeneralisation, Set<ClassFrameGroundGeneralisation>>
                entry : reg2instH.entrySet()) {
            Set<OWLClassExpression> ces = new HashSet<>();
            for(ClassFrameGroundGeneralisation f : entry.getValue()){
                ces.add(f.getClassExpression());
            }
            reg2inst.put(entry.getKey(), ces); 
        } 


        SetRegularityHierarchy hierarchy = new SetRegularityHierarchy(reg2inst); 
        hierarchy.writeGraphWithInstances(outputPath + "/" + ontologyName);

        writeInstances(reg2inst, frameMiner, hierarchy,outputPath + "/" + ontologyName);

        //writeInstances(nodes, outputPath + "/" + ontologyName);
        //writeStatistics(nodes, outputPath + "/" + ontologyName);

    }

    public static void writeInstances(
            Map<ClassFrameGroundGeneralisation,Set<OWLClassExpression>> reg2insts,
            ClassFrameGGMiner frameMiner, 
            SetRegularityHierarchy hierarchy,
            String output) throws Exception {

        //create folder for 'instances'
        //iterate over all regularities and create folder for each regularity
        //create an owl ontology for each instance

        String basePath = output + "/instances";
        IOHelper.createFolder(basePath);
        Map<OWLClassExpression,ClassFrame> class2frame = frameMiner.getClass2Frame();

        for(Map.Entry<ClassFrameGroundGeneralisation, Set<OWLClassExpression>>
                entry : reg2insts.entrySet()){

            ClassFrameGroundGeneralisation reg = entry.getKey();
            int regularityID = hierarchy.getClassFrameID(reg);

            String regularityPath = basePath + "/" + regularityID;
            IOHelper.createFolder(regularityPath);

            int instanceID = 1;
            for(OWLClassExpression i : entry.getValue()){
                Set<OWLAxiom> axioms = class2frame.get(i).getAxioms();
                OntologySaver.saveAxioms(axioms, regularityPath + "/" + instanceID + ".owl"); 
                instanceID++;
            }

        }
    }

    /*
    public static void writeStatistics(Set<HierarchyNode> nodes, String output) throws Exception {
        String basePath = output + "/statistics";
        String header = "Regularity ID," +
                        "Number Of Instances," +
                        "Size of Regularity Structure," +
                        "Depth" +
                        "Number of Leafs," +
                        "Number of Non-Leafs," +
                        "Max Branching, "+ 
                        "Average Branching";

        IOHelper.writeAppend("", basePath); 
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
    */

}
