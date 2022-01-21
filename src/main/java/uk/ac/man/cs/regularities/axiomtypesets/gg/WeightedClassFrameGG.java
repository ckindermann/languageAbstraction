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


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

public class WeightedClassFrameGG {
    private OWLClassExpression entity;

    private HashMap<SyntaxTree,Integer> superclasses;//each tree occurrs a number of times
    private HashMap<SyntaxTree,Integer> equivalences;
    private HashMap<SyntaxTree,Integer> disjointUnion;
    private HashMap<SyntaxTree,Integer> disjointClasses;

    private int size;//number of axioms
    private int chunks;//number of non-isomorphic axioms

    private TreeMap<Integer,Set<SyntaxTree>> weight2superclasses;
    private TreeMap<Integer,Set<SyntaxTree>> weight2equivalences;
    private TreeMap<Integer,Set<SyntaxTree>> weight2disjointUnion;
    private TreeMap<Integer,Set<SyntaxTree>> weight2disjointClasses;

    private SyntaxTreeBuilder treeBuilder;

    public WeightedClassFrameGG(ClassFrame frame){
        this.treeBuilder = new SyntaxTreeBuilder();//full syntax tree builder
        this.initialise(frame); 
        this.chunks = this.superclasses.size() +
                      this.equivalences.size() + 
                      this.disjointUnion.size() +
                      this.disjointClasses.size();
        this.size = this.chunks;

        //this.size = frame.getSuperClasses().size() +  
        //            frame.getEquivalentClasses().size() + 
        //            frame.getDisjointUnion().size() + 
        //            frame.getDisjointClasses().size();
    }

    public String toString(){
        String res = "Superclasses \n";
        for(SyntaxTree t : this.superclasses.keySet()){
            res += "\t" + t.getRoot().toString() + "[" + superclasses.get(t) + "]" + "\n";
        }
        res += "Equivalences \n";
        for(SyntaxTree t : this.equivalences.keySet()){
            res += "\t" + t.getRoot().toString() + "[" + equivalences.get(t) + "]" + "\n";
        }
        res += "DisjointUnion \n";
        for(SyntaxTree t : this.disjointUnion.keySet()){
            res += "\t" + t.getRoot().toString() + "[" + disjointUnion.get(t) + "]" + "\n";
        }
        res += "DisjointClasses \n";
        for(SyntaxTree t : this.disjointClasses.keySet()){
            res += "\t" + t.getRoot().toString() + "[" + disjointClasses.get(t) + "]" + "\n";
        }
        return res;

    }

    public TreeMap<Integer,Set<SyntaxTree>> getWeight2superclasses(){
        return this.weight2superclasses;
    }
    public TreeMap<Integer,Set<SyntaxTree>> getWeight2equivalences(){
        return this.weight2equivalences;
    }
    public TreeMap<Integer,Set<SyntaxTree>> getWeight2disjointUnion(){
        return this.weight2disjointUnion;
    }
    public TreeMap<Integer,Set<SyntaxTree>> getWeight2disjointClasses(){
        return this.weight2disjointClasses;
    }
    public Map<SyntaxTree,Integer> getSuperClasses(){
        return this.superclasses;
    }
    public Map<SyntaxTree,Integer> getEquivalences(){
        return this.equivalences;
    }
    public Map<SyntaxTree,Integer> getDisjointUnions(){
        return this.disjointUnion;
    }
    public Map<SyntaxTree,Integer> getDisjointClasses(){
        return this.disjointClasses;
    }
    public Map<SyntaxTree,Integer> getTrees(){ 
        Map<SyntaxTree,Integer> res = new HashMap<>();
        res.putAll(this.superclasses);
        res.putAll(this.equivalences);
        res.putAll(this.disjointUnion);
        res.putAll(this.disjointClasses);
        return res;
    }

    private void initialise(ClassFrame d){
        this.entity = d.getClassExpression();

        this.superclasses = new HashMap<>();
        this.equivalences = new HashMap<>();
        this.disjointUnion = new HashMap<>();
        this.disjointClasses = new HashMap<>();

        this.initMap(this.superclasses, new HashSet<OWLAxiom>(d.getSuperClasses()));
        this.initMap(this.equivalences, new HashSet<OWLAxiom>(d.getEquivalentClasses()));
        this.initMap(this.disjointUnion, new HashSet<OWLAxiom>(d.getDisjointUnion()));
        this.initMap(this.disjointClasses, new HashSet<OWLAxiom>(d.getDisjointClasses()));

        this.weight2superclasses = new TreeMap<>();
        this.weight2equivalences = new TreeMap<>();
        this.weight2disjointUnion = new TreeMap<>();
        this.weight2disjointClasses = new TreeMap<>();

        this.initTreeMap(this.weight2superclasses, this.superclasses);
        this.initTreeMap(this.weight2equivalences, this.equivalences);
        this.initTreeMap(this.weight2disjointUnion, this.disjointUnion);
        this.initTreeMap(this.weight2disjointClasses, this.disjointClasses); 
    }

    public OWLClassExpression getClassExpression(){
        return this.entity;
    }

    public int size(){
        return this.size;
    }

    public int chunks(){
        return this.chunks;
    }


    public boolean isIsomorphic(WeightedClassFrameGG f){
        if(f.chunks() != this.chunks)
            return false;

        //iterate over superclasses
        if(!isIsomorphic(this.superclasses, f.getSuperClasses()))
            return false;
        if(!isIsomorphic(this.equivalences, f.getEquivalences()))
            return false;
        if(!isIsomorphic(this.disjointUnion, f.getDisjointUnions()))
            return false;
        if(!isIsomorphic(this.disjointClasses, f.getDisjointClasses()))
            return false; 

        return true;
    }

    private boolean isIsomorphic(Map<SyntaxTree,Integer> here, 
            Map<SyntaxTree,Integer> there){ 
        return SetGroundGeneralisation.exists(here.keySet(), there.keySet()); 
    }

    public boolean coveredBy(WeightedClassFrameGG f){
        if(!coveredBy(this.getSuperClasses(), f.getSuperClasses())){
            return false;
        }
        if(!coveredBy(this.getEquivalences(), f.getEquivalences())){
            return false;
        }
        if(!coveredBy(this.getDisjointUnions(), f.getDisjointUnions())){
            return false;
        }
        if(!coveredBy(this.getDisjointClasses(), f.getDisjointClasses())){
            return false;
        }
        return true; 
    } 

    private boolean coveredBy(Map<SyntaxTree,Integer> smaller, Map<SyntaxTree,Integer> bigger){ 
        for(SyntaxTree t : smaller.keySet()){ 
            boolean covered = false; 

            for(SyntaxTree ct : bigger.keySet()){ 
                //expression tree needs to be equivalent
                if(GroundGeneralisation.exists(t,ct)){
                    covered = true;
                    break;
                } 
            } 
            if(!covered){
                return false;//one tree is not covered
            } 
        }
        return true; 
    }

    //private boolean coversSuperclasses(WeightedClassFrameGG f){
    //    for(SyntaxTree t : this.superclasses.keySet()){ 
    //        boolean covered = false;

    //        int tWeight = this.superclasses.get(t);

    //        for(SyntaxTree ct : f.getSuperClasses().keySet()){
    //            int ctWeight = f.getSuperClasses().get(ct);

    //            //weight needs to be covering
    //            if(ctWeight >= tWeight){
    //                //expression tree needs to be equivalent
    //                if(GroundGeneralisation.exists(t,ct)){//ATTENTION
    //                    covered = true;
    //                    break;
    //                } 
    //            } 
    //        } 
    //        if(!covered){
    //            return false;//one tree is not covered
    //        }
    //    } 
    //    return true; 
    //}

    private void initMap(Map<SyntaxTree,Integer> map, Set<OWLAxiom> set){
        for(OWLAxiom a : set){
            SyntaxTree insert = this.treeBuilder.build(a);
            boolean found = false;
            for(Map.Entry<SyntaxTree,Integer> entry : map.entrySet()){
                SyntaxTree t = entry.getKey();
                int weight = entry.getValue();
                if(GroundGeneralisation.exists(t,insert)){//ATTENTION
                    found = true;
                    map.replace(t,weight+1); 
                    break;
                }
            }
            if(!found){
                map.put(insert,1);
            } 
        }
    }

    private void initTreeMap(TreeMap<Integer,Set<SyntaxTree>> tm, Map<SyntaxTree,Integer> m){
        for(Map.Entry<SyntaxTree,Integer> entry : m.entrySet()){
            SyntaxTree t = entry.getKey();
            int weight = entry.getValue();
            tm.putIfAbsent(weight,new HashSet<>());
            tm.get(weight).add(t);
        }
    }

}
