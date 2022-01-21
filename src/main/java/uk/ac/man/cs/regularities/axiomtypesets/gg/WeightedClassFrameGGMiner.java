package uk.ac.man.cs.regularities.axiomtypesets.gg;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.gg.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


public class WeightedClassFrameGGMiner {

    private OWLOntology ontology;
    private TreeMap<Integer,Set<WeightedClassFrameGG>> specificity2frames;
    private TreeMap<Integer,Set<WeightedClassFrameGG>> specificity2regularity;

    private Map<WeightedClassFrameGG,WeightedClassFrameGG> instance2regularity;
    private Map<WeightedClassFrameGG,Set<WeightedClassFrameGG>> regularity2instances;

    private Map<OWLClassExpression,ClassFrame> class2frame; 

    public WeightedClassFrameGGMiner(OWLOntology o){
        this.ontology = o;
        ClassFrameMiner cfminer = new ClassFrameMiner(o);
        this.class2frame = cfminer.getFrames();
        this.stratifyBySize(new HashSet<>(this.class2frame.values()));//is this inefficient?
        this.mine(); 
    }

    public Map<WeightedClassFrameGG,Set<WeightedClassFrameGG>> getRegularity2instance(){
        return this.regularity2instances;
    }

    public Map<OWLClassExpression,ClassFrame> getClass2Frame(){
        return this.class2frame;
    }

    private void mine() {
        this.specificity2regularity = new TreeMap<>();

        this.instance2regularity = new HashMap<>();
        this.regularity2instances = new HashMap<>();

        //System.out.println("Work : " + this.specificity2frames.size());
        int i = 1;
        for(Map.Entry<Integer,Set<WeightedClassFrameGG>> entry : this.specificity2frames.entrySet()){

            //System.out.println("Done : " + i++);

            int specificity = entry.getKey();//get stratum identifier
            Set<WeightedClassFrameGG> toPartition = entry.getValue();//get stratum

            this.specificity2regularity.putIfAbsent(specificity,new HashSet<>());
            Set<WeightedClassFrameGG> regs = this.specificity2regularity.get(specificity);

            for(WeightedClassFrameGG frame : toPartition){
                boolean found = false;
                for(WeightedClassFrameGG regularity : regs){
                    if(frame.isIsomorphic(regularity)){
                        this.instance2regularity.put(frame,regularity);
                        this.regularity2instances.get(regularity).add(frame);
                        found = true; 
                        break;
                    } 
                } 
                if(!found){
                    this.regularity2instances.put(frame,new HashSet<>());
                    this.regularity2instances.get(frame).add(frame);
                    regs.add(frame);
                }
            } 
        }

    }

    private void stratifyBySize(Set<ClassFrame> framesToTest){
        this.specificity2frames = new TreeMap<>(); 
        for(ClassFrame f : framesToTest){
            WeightedClassFrameGG fr = new WeightedClassFrameGG(f);
            int specificity = this.getSpecificity(fr);
            this.specificity2frames.putIfAbsent(specificity, new HashSet<>());
            this.specificity2frames.get(specificity).add(fr);
        }
    }

    private int getSpecificity(WeightedClassFrameGG f){ 
        int specificity = 0;

        Map<SyntaxTree,Integer> tree2weight = f.getTrees();

        for(Map.Entry<SyntaxTree,Integer> entry : tree2weight.entrySet()){
            SyntaxTree t = entry.getKey();
            specificity += (getSpecificity(t));
        } 
        return specificity;
    }

    private int getSpecificity(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();
        return graph.vertexSet().size(); 
    } 
}

