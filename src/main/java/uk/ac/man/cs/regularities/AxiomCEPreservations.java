package uk.ac.man.cs.regularities;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.cep.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
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


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class AxiomCEPreservations {

    private OWLOntology ontology;

    private TreeMap<Integer,Set<SyntaxTree>> specificity2tree;
    private TreeMap<Integer,Set<SyntaxTree>> specificity2regularity;

    private Map<OWLAxiom,SyntaxTree> axiom2regularity;
    private Map<SyntaxTree,Set<OWLAxiom>> regularity2instances;

    //NB: the abstraction is done via the parser
    private CEPreservanceBuilder treeBuilder;

    public AxiomCEPreservations(OWLOntology o) throws Exception {
        this.ontology = o;
        this.treeBuilder = new CEPreservanceBuilder();
        this.stratify(); 
        this.mine();
    }

    public Map<OWLAxiom,SyntaxTree> getAxiom2regularity(){
        return this.axiom2regularity;
    }

    public Map<SyntaxTree,Set<OWLAxiom>> getRegularity2instance(){
        return this.regularity2instances;
    }

    private void mine() throws Exception {
        this.specificity2regularity = new TreeMap<>();

        this.axiom2regularity = new HashMap();
        this.regularity2instances = new HashMap();

        //System.out.println("Work to do: " + this.specificity2tree.size());
        int i = 1;

        for(Map.Entry<Integer,Set<SyntaxTree>> entry : this.specificity2tree.entrySet()){
            int specificity = entry.getKey();//get stratum identifier
            Set<SyntaxTree> toPartition = entry.getValue();//get stratum
            //System.out.println("Working on: " + i++ + " with " + toPartition.size());

            this.specificity2regularity.putIfAbsent(specificity,new HashSet<>());
            Set<SyntaxTree> regs = this.specificity2regularity.get(specificity);

            for(SyntaxTree t : toPartition){
                OWLAxiom a = ((AxiomNode) t.getRoot()).getAxiom();
                boolean found = false;
                for(SyntaxTree r : regs){
                    if(ClassExpressionPreservance.exists(t,r)){
                        this.axiom2regularity.put(a,r);
                        this.regularity2instances.get(r).add(a); 
                        found = true;
                        break;
                    }
                } 
                if(!found){
                    this.regularity2instances.put(t,new HashSet<>());
                    this.regularity2instances.get(t).add(a);
                    regs.add(t); 
                }
            }
        } 
    }

    private void stratify(){
        Set<OWLAxiom> toTest = new HashSet<>();
        toTest.addAll(this.ontology.getAxioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED));
        toTest.addAll(this.ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED));
        toTest.addAll(this.ontology.getAxioms(AxiomType.DISJOINT_UNION, Imports.INCLUDED));
        toTest.addAll(this.ontology.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.INCLUDED));

        this.specificity2tree = new TreeMap();
        for(OWLAxiom a : toTest){
            SyntaxTree t = this.treeBuilder.build(a);
            int specificity = this.getSpecificity(t);
            specificity2tree.putIfAbsent(specificity, new HashSet<>());
            specificity2tree.get(specificity).add(t);
        }
    }

    private int getSpecificity(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();
        return graph.vertexSet().size(); 
    } 

}
