package uk.ac.man.cs.regularities.axiomsets.renaming;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.renaming.*;
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


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class ClassFrameRenamingMiner {

    private OWLOntology ontology;
    private Map<ClassFrameRenamingStratum,Set<ClassFrameRenaming>> stratum2frames;
    private Map<ClassFrameRenamingStratum,Set<ClassFrameRenaming>> stratum2regularity;

    private Map<ClassFrameRenaming,ClassFrameRenaming> instance2regularity;
    private Map<ClassFrameRenaming,Set<ClassFrameRenaming>> regularity2instances;


    public ClassFrameRenamingMiner(OWLOntology o){
        this.ontology = o;
        ClassFrameMiner cfminer = new ClassFrameMiner(o);
        Map<OWLClassExpression,ClassFrame> map = cfminer.getFrames();
        //this.stratifyBySize(new HashSet<>(map.values()));//is this inefficient?
        this.stratify(new HashSet<>(map.values()));//is this inefficient?
        this.mine(); 
    }

    public Map<ClassFrameRenaming,Set<ClassFrameRenaming>> getRegularity2instance(){
        return this.regularity2instances;
    }

    private void mine() {
        this.stratum2regularity = new HashMap<>();

        this.instance2regularity = new HashMap<>();
        this.regularity2instances = new HashMap<>();

        //TODO: parralelise this

        //pass Set<ClassFrameRenaming>
        //pass instance2regularity
        //pass regularity2instances
        System.out.println("Work : " + this.stratum2frames.size());
        int i = 1;
        for(Map.Entry<ClassFrameRenamingStratum,Set<ClassFrameRenaming>> entry : this.stratum2frames.entrySet()){


            ClassFrameRenamingStratum stratum = entry.getKey();//get stratum identifier
            Set<ClassFrameRenaming> toPartition = entry.getValue();//get stratum 

            this.stratum2regularity.putIfAbsent(stratum,new HashSet<>());
            Set<ClassFrameRenaming> regs = this.stratum2regularity.get(stratum);
            System.out.println("toPartition next : " + toPartition.size());

            for(ClassFrameRenaming frame : toPartition){
                boolean found = false;
                for(ClassFrameRenaming regularity : regs){
                    if(frame.isRenaming(regularity)){
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
            System.out.println("Done : " + i++);
            System.out.println("Regs found : " + regs.size());

            /*
            if(i-1 == 20){
                for(ClassFrameRenaming r : regs){
                    System.out.println("================");
                    System.out.println("REGULARITY : " + r.toString());
                    for(ClassFrameRenaming s : this.regularity2instances.get(r)){
                        System.out.println("Instance : " + s.toString()); 
                    } 
                    System.out.println("================");
                }
            }*/
        } 
    }

    private void stratify(Set<ClassFrame> framesToTest){
        this.stratum2frames = new HashMap<>();
        for(ClassFrame f : framesToTest){
            ClassFrameRenaming fr = new ClassFrameRenaming(f);
            ClassFrameRenamingStratum stratum = new ClassFrameRenamingStratum(fr);
            this.stratum2frames.putIfAbsent(stratum, new HashSet<>());
            this.stratum2frames.get(stratum).add(fr); 
        }

    }

    //private void stratifyBySize(Set<ClassFrame> framesToTest){
    //    this.specificity2frames = new TreeMap<>(); 
    //    for(ClassFrame f : framesToTest){
    //        ClassFrameRenaming fr = new ClassFrameRenaming(f);
    //        int specificity = this.getSpecificity(fr);
    //        this.specificity2frames.putIfAbsent(specificity, new HashSet<>());
    //        this.specificity2frames.get(specificity).add(fr);
    //    }
    //}

    //private int getSpecificity(ClassFrameRenaming f){ 
    //    int specificity = 0;
    //    for(SyntaxTree t : f.getTrees()){
    //        specificity += getSpecificity(t);
    //    } 
    //    return specificity;
    //}

    //private int getSpecificity(SyntaxTree t){
    //    SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();
    //    return graph.vertexSet().size(); 
    //} 
}

