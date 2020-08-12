package uk.ac.man.cs.regularities.axiomsets.cep;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.cep.*;
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


public class ClassFrameCEPMiner {

    private OWLOntology ontology;
    private TreeMap<Integer,Set<ClassFrameCEPreservation>> specificity2frames;
    private TreeMap<Integer,Set<ClassFrameCEPreservation>> specificity2regularity;

    private Map<ClassFrameCEPreservation,ClassFrameCEPreservation> instance2regularity;
    private Map<ClassFrameCEPreservation,Set<ClassFrameCEPreservation>> regularity2instances;

    private Map<OWLClassExpression,ClassFrame> class2frame; 

    public ClassFrameCEPMiner(OWLOntology o){
        this.ontology = o;
        ClassFrameMiner cfminer = new ClassFrameMiner(o);
        this.class2frame = cfminer.getFrames();
        this.stratifyBySize(new HashSet<>(this.class2frame.values()));//is this inefficient?
        this.mine(); 
    }

    public Map<ClassFrameCEPreservation,Set<ClassFrameCEPreservation>> getRegularity2instance(){
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
        for(Map.Entry<Integer,Set<ClassFrameCEPreservation>> entry : this.specificity2frames.entrySet()){

            //System.out.println("Done : " + i++);

            int specificity = entry.getKey();//get stratum identifier
            Set<ClassFrameCEPreservation> toPartition = entry.getValue();//get stratum

            this.specificity2regularity.putIfAbsent(specificity,new HashSet<>());
            Set<ClassFrameCEPreservation> regs = this.specificity2regularity.get(specificity);

            for(ClassFrameCEPreservation frame : toPartition){
                boolean found = false;
                for(ClassFrameCEPreservation regularity : regs){
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
            ClassFrameCEPreservation fr = new ClassFrameCEPreservation(f);
            int specificity = this.getSpecificity(fr);
            this.specificity2frames.putIfAbsent(specificity, new HashSet<>());
            this.specificity2frames.get(specificity).add(fr);
        }
    }

    private int getSpecificity(ClassFrameCEPreservation f){ 
        int specificity = 0;
        Map<SyntaxTree,Integer> tree2weight = f.getTrees();
        for(Map.Entry<SyntaxTree,Integer> entry : tree2weight.entrySet()){
            SyntaxTree t = entry.getKey();
            int w = entry.getValue();
            specificity += (w*getSpecificity(t));
        } 
        return specificity;
    }

    private int getSpecificity(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();
        return graph.vertexSet().size(); 
    } 
}

