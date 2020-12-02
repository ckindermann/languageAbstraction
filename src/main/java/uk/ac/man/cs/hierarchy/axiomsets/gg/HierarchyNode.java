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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

//TODO: parameterise according to isomorphism/subisomporhisn

public class HierarchyNode {
    private int ID;
    private ClassFrameGroundGeneralisation classFrame; 
    private Set<OWLClassExpression> instances;

    //private SyntaxTree tree;//tree corresponding to the axiom
    //private SyntaxTree internalTree; //tree without 'original' leafs 
    //private Set<OWLAxiom> instances;

    private Set<HierarchyNode> children;
    private Set<HierarchyNode> parents;

    public Set<HierarchyNode> visitedDuringInsertion;

    public HierarchyNode(ClassFrameGroundGeneralisation f, Set<OWLClassExpression> i, int id){
        this.ID = id;
        this.classFrame = f;
        this.instances = new HashSet<>();
        this.instances.addAll(i);

        this.children = new HashSet<>();
        this.parents = new HashSet<>();

        this.visitedDuringInsertion = new HashSet<>(); 
    }

    public ClassFrameGroundGeneralisation getFrame(){
        return this.classFrame;
    }

    public Set<OWLClassExpression> getInstances(){
        return this.instances;
    }

    public Set<HierarchyNode> getChildren(){
        return this.children;
    }

    public void setID(int id){
        this.ID = id;
    }

    public int getID(){
        return this.ID;
    }

    public void addInstance(OWLClassExpression ce){
        this.instances.add(ce);
    }

    public void addInstances(Set<OWLClassExpression> ces){
        this.instances.addAll(ces);
    }

    public void addChild(HierarchyNode node){
        this.children.add(node);
    }

    public void addChildren(Set<HierarchyNode> nodes){
        this.children.addAll(nodes);
    }

    public void addParent(HierarchyNode node){
        this.parents.add(node);
    }

    public void addParents(Set<HierarchyNode> nodes){
        this.parents.addAll(nodes);
    }

    public boolean insertFromAbove(HierarchyNode n){
        n.visitedDuringInsertion.add(this); 

        //if(this.sameAs(n)){//should never happen..
        //   n.addInstance(((AxiomNode) this.tree.getRoot()).getAxiom());
        //   return true;
        //}

        if(this.coveredBy(n)){
            boolean insertionPointFound = false;
            for(HierarchyNode c : this.children){
                if(!n.visitedDuringInsertion.contains(c)){
                    if(c.insertFromAbove(n)){
                        insertionPointFound = true;
                    } 
                } else {
                    if(c.coveredBy(n)){//the node to be inserted can be reached from somewhere else 
                        insertionPointFound = true;
                    } 
                } 
            }
            if(!insertionPointFound){
                this.children.add(n);
            }
            return true; 
        }
        return false; 
    }

    //if this tree is a 'subgraph' in the other tree
    public boolean coveredBy(HierarchyNode n){
        //boolean res = Subisomorphism.exists(n.getInternalTree(), this.internalTree);//check whether this is correct - should be though
        boolean res = this.classFrame.coveredBy(n.getFrame());
        //System.out.println("Stop");
        return res;
        //return Subisomorphism.exists(this.internalTree, n.getInternalTree());
    } 

    public boolean sameAs(HierarchyNode n){
        return this.classFrame.isIsomorphic(n.getFrame());
    }
}
