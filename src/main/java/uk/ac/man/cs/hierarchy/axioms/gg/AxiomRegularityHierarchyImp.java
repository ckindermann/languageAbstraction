package uk.ac.man.cs.hierarchy.axioms.gg;

import uk.ac.man.cs.ont.*;
//import uk.ac.man.cs.pat.*;
//import uk.ac.man.cs.profile.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;//has syntaxTrees

import uk.ac.man.cs.parser.*;

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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


import java.nio.file.*;

import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

//ATTENTION: this expects distinct (non-isomorpphic} regularities as input
//NOte: this is easy to change
public class AxiomRegularityHierarchyImp {


    private Map<SyntaxTree,HierarchyNode> instance2node;
    private Map<HierarchyNode,Set<SyntaxTree>> node2instances;//regularity2instances
    private TreeMap<Integer,Set<HierarchyNode>> specificity2node;//specificity2regulartiy

    private OWLOntology ontology;
    private Map<Integer, HierarchyNode> id2node;
    private Map<HierarchyNode, Integer> node2id;

    private Set<HierarchyNode> nodes;
    private Set<HierarchyNode> roots;

    private int nextID;

    public AxiomRegularityHierarchyImp(Set<SyntaxTree> regularities){
        this.initialise(regularities);
        this.buildHierarchy(); 
        //System.out.println("Number of regularities " + regularities.size());
        //System.out.println("Number of roots " + roots.size()); 
    }

    private void initialise(Set<SyntaxTree> trees){ 
        this.nextID = 1;
        this.id2node = new HashMap<>();
        this.node2id = new HashMap<>();
        this.nodes = new HashSet<>();
        this.instance2node = new HashMap<>();
        //stratify syntax trees by size 
        TreeMap<Integer,Set<SyntaxTree>> specificity2tree = new TreeMap();
        for(SyntaxTree t : trees){
            int specificity = this.getSpecificity(t);
            specificity2tree.putIfAbsent(specificity, new HashSet<>());
            specificity2tree.get(specificity).add(t);
        }

        ConstructorPreservanceBuilder builder = new ConstructorPreservanceBuilder(); 
        //identify equivalence classes in each stratum
        this.node2instances = new HashMap<>();
        this.specificity2node = new TreeMap<>();
        this.nextID = 1;
        for(Map.Entry<Integer,Set<SyntaxTree>> entry : specificity2tree.entrySet()){
            int specificity = entry.getKey();//get stratum identifier
            Set<SyntaxTree> toPartition = entry.getValue();//get stratum
            this.specificity2node.putIfAbsent(specificity, new HashSet<>());
            Set<HierarchyNode> reg = this.specificity2node.get(specificity);//equivalence clases
            for(SyntaxTree t : toPartition){
                boolean found = false;
                for(HierarchyNode n : reg){
                    if(n.represents(t)){
                        this.instance2node.put(t,n);
                        this.node2instances.get(n).add(t);
                        n.addInstance(((AxiomNode) t.getRoot()).getAxiom());
                        found = true;
                        break;
                    }
                } 
                if(!found){
                    SyntaxTree internal = builder.build(((AxiomNode) t.getRoot()).getAxiom()); 
                    HierarchyNode node = new HierarchyNode(t,internal,nextID); 
                    this.node2instances.put(node,new HashSet<>());

                    reg.add(node);
                    this.nodes.add(node); 
                    this.id2node.put(nextID,node);
                    this.node2id.put(node,nextID);
                    this.nextID++; 
                }
            }
        }
    } 

    public Set<HierarchyNode> getRoots(){
        return this.roots;
    }

    public Set<HierarchyNode> getNodes(){
        return this.nodes;
    }

    private int getSpecificity(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();
        return graph.vertexSet().size(); 
    }

    private void buildHierarchy(){
        this.roots = new HashSet<>();
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
            IOHelper.writeAppend(node.getTree().getRoot().toString(),output + "/" + node.getID());
        }
        for(HierarchyNode r : this.roots){
            IOHelper.writeAppend("Root " + r.getID(), output + "/roots"); 
        }
    }

}
