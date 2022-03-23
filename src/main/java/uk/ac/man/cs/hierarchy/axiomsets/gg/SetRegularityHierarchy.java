package uk.ac.man.cs.hierarchy.axiomsets.gg;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.structure.*;//has syntaxTrees
import uk.ac.man.cs.structure.nodes.*;//has syntaxTrees
import uk.ac.man.cs.iso.gg.*;
import uk.ac.man.cs.subIso.*;
import uk.ac.man.cs.regularities.axiomsets.gg.*;

//import uk.ac.man.cs.pat.*;
//import uk.ac.man.cs.profile.*;
//import uk.ac.man.cs.iso.*;
//import uk.ac.man.cs.parser.*;

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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

//TODO: parameterise according to isomorphism/subisomporhisn

public class SetRegularityHierarchy {

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String output = args[1];

        File ontFile = new File(ontFilePath);
        //log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        ClassFrameGGMiner frameMiner = new ClassFrameGGMiner(ont);
        Map<ClassFrameGroundGeneralisation,Set<ClassFrameGroundGeneralisation>> reg2instH = frameMiner.getRegularity2instance();

        //conversion for instances
        //(this just takes ClassFrames and converts them to their associated LHS entities)
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
        hierarchy.writeGraphWithInstances(output);
    }

    private Map<HierarchyNode,ClassFrameGroundGeneralisation> node2frame;
    private Map<ClassFrameGroundGeneralisation,HierarchyNode> frame2node; 
    private Map<ClassFrameGroundGeneralisation, Set<OWLClassExpression>> frame2instance;

    private Map<Integer, HierarchyNode> id2node;
    private Map<HierarchyNode, Integer> node2id;

    private Set<HierarchyNode> nodes;
    private Set<HierarchyNode> roots;

    private TreeMap<Integer,Set<HierarchyNode>> specificity2node;//specificity2regulartiy

    //note: we assume the input to be regularities - they are all non-isomorphic
    //all that needs to happen is to organise them in a hierarchy
    public SetRegularityHierarchy(Map<ClassFrameGroundGeneralisation, Set<OWLClassExpression>> reg2inst){
        this.node2frame = new HashMap<>();
        this.frame2node = new HashMap<>();
        this.frame2instance = new HashMap<>();

        this.id2node = new HashMap<>();
        this.node2id = new HashMap<>();

        this.nodes = new HashSet<>();
        this.roots = new HashSet<>(); 

        this.specificity2node = new TreeMap<>();

        int id = 1;

        for (Map.Entry<ClassFrameGroundGeneralisation, Set<OWLClassExpression>> entry : reg2inst.entrySet()) {
            HierarchyNode n = new HierarchyNode(entry.getKey(), entry.getValue(), id);
            this.id2node.put(id,n);
            this.node2id.put(n,id);
            this.node2frame.put(n,entry.getKey());
            this.frame2node.put(entry.getKey(), n);
            this.frame2instance.put(entry.getKey(), entry.getValue()); 
            this.nodes.add(n);
            int specificity = entry.getKey().size();
            this.specificity2node.putIfAbsent(specificity, new HashSet<>());
            this.specificity2node.get(specificity).add(n);
            id++;
        } 
        this.buildHierarchy();
    }

    private void buildHierarchy(){
        //this.roots = new HashSet<>();
        for(Map.Entry<Integer,Set<HierarchyNode>> entry : this.specificity2node.entrySet()){
            Integer specificity = entry.getKey();
            Set<HierarchyNode> nodes2insert = entry.getValue();
            System.out.println("Inserting Level " + specificity + " with " + nodes2insert.size());

            for(HierarchyNode n : nodes2insert){
                boolean insertionPointFound = false;
                for(HierarchyNode r : this.roots){
                    if(r.insertFromAbove(n)){
                        insertionPointFound = true;
                    }
                }
                if(!insertionPointFound){
                    //System.out.println("Root " + n.getID());
                    this.roots.add(n);
                } 
            }
        } 
    } 

    public Set<HierarchyNode> getNodes(){
        return this.nodes;
    }

    public Set<HierarchyNode> getRoots(){
        return this.roots;
    }

    public void writeGraphWithInstances(String output){
        IOHelper.writeAppend("digraph gname {", output + "/graph");

        //first write labels 
        for(HierarchyNode node : this.nodes){
            String label = node.getID() + " [label=\"" + node.getID() + "\\n" + node.getInstances().size() + "\"]";
            IOHelper.writeAppend(label, output + "/graph");
        } 

        writeGraph(output);

        IOHelper.writeAppend("}", output + "/graph"); 
    }

    public void writeGraph(String output){
        for(HierarchyNode node : this.nodes){
            for(HierarchyNode c : node.getChildren()){ 
                String edge = node.getID() + " -> " + c.getID();
                IOHelper.writeAppend(edge, output + "/graph");
            }
            //IOHelper.writeAppend(node.getFrame().toString(),output + "/" + node.getID());
        }
        //for(HierarchyNode r : this.roots){
        //    IOHelper.writeAppend("Root " + r.getID(), output + "/roots"); 
        //}
    }

    public int getClassFrameID(ClassFrameGroundGeneralisation f){
        HierarchyNode node = this.frame2node.get(f);
        int id = this.node2id.get(node); 
        return id;
    }
}
