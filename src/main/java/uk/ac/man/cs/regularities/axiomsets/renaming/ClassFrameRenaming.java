package uk.ac.man.cs.regularities.axiomsets.renaming;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.renaming.*;
//import uk.ac.man.cs.iso.gg.*;
import uk.ac.man.cs.iso.irig.*;
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

public class ClassFrameRenaming {
    private OWLClassExpression entity;

    private Set<SyntaxTree> superclasses;//each tree occurrs a number of times
    private Set<SyntaxTree> equivalences;
    private Set<SyntaxTree> disjointUnion;
    private Set<SyntaxTree> disjointClasses;

    private int superclassSpecificity;
    private int equivalenceSpecificity;
    private int disjointUnionSpecificity;
    private int disjointClassesSpecificity;

    private SyntaxTreeBuilder treeBuilder;

    public ClassFrameRenaming(ClassFrame frame){
        this.treeBuilder = new SyntaxTreeBuilder();//full syntax tree builder
        this.initialise(frame); 
        this.initialiseSpecificity();
    }

    public String toString(){
        String res = "Superclasses \n";
        for(SyntaxTree t : this.superclasses){
            res += "\t" + t.getRoot().toString() + "\n";
        }
        res += "Equivalences \n";
        for(SyntaxTree t : this.equivalences){
            res += "\t" + t.getRoot().toString() + "\n";
        }
        res += "DisjointUnion \n";
        for(SyntaxTree t : this.disjointUnion){
            res += "\t" + t.getRoot().toString() + "\n";
        }
        res += "DisjointClasses \n";
        for(SyntaxTree t : this.disjointClasses){
            res += "\t" + t.getRoot().toString() + "\n";
        }
        return res;
    }

    public int getSuperClassSpecificity(){
        return this.superclassSpecificity;
    }
    public int getEquivalenceSpecificity(){
        return this.equivalenceSpecificity;
    }
    public int getDisjointUnionSpecificity(){
        return this.disjointUnionSpecificity;
    }
    public int getDisjointClassesSpecificity(){
        return this.disjointClassesSpecificity;
    }

    public Set<SyntaxTree> getSuperClasses(){
        return this.superclasses;
    }
    public Set<SyntaxTree> getEquivalences(){
        return this.equivalences;
    }
    public Set<SyntaxTree> getDisjointUnions(){
        return this.disjointUnion;
    }
    public Set<SyntaxTree> getDisjointClasses(){
        return this.disjointClasses;
    }

    public Set<SyntaxTree> getTrees(){ 
        Set<SyntaxTree> res = new HashSet<>();
        res.addAll(this.superclasses);
        res.addAll(this.equivalences);
        res.addAll(this.disjointUnion);
        res.addAll(this.disjointClasses);
        return res;
    }

    private void initialise(ClassFrame d){
        this.entity = d.getClassExpression();

        this.superclasses = new HashSet<>();
        this.equivalences = new HashSet<>();
        this.disjointUnion = new HashSet<>();
        this.disjointClasses = new HashSet<>();

        this.initSet(this.superclasses, new HashSet<OWLAxiom>(d.getSuperClasses()));
        this.initSet(this.equivalences, new HashSet<OWLAxiom>(d.getEquivalentClasses()));
        this.initSet(this.disjointUnion, new HashSet<OWLAxiom>(d.getDisjointUnion()));
        this.initSet(this.disjointClasses, new HashSet<OWLAxiom>(d.getDisjointClasses())); 
    }

    private void initialiseSpecificity(){
        this.superclassSpecificity = this.getSpecificity(this.superclasses);
        this.equivalenceSpecificity = this.getSpecificity(this.equivalences);
        this.disjointUnionSpecificity = this.getSpecificity(this.disjointUnion);
        this.disjointClassesSpecificity = this.getSpecificity(this.disjointClasses); 
    }

    private void initSet(Set<SyntaxTree> trees, Set<OWLAxiom> axioms){
        for(OWLAxiom a : axioms){ 
            trees.add(this.treeBuilder.build(a));
        } 
    }

    public OWLClassExpression getClassExpression(){
        return this.entity;
    }

    public boolean isRenaming(ClassFrameRenaming f){

        if(this.superclasses.size() != f.getSuperClasses().size())
            return false;
        if(this.equivalences.size() != f.getEquivalences().size())
            return false;
        if(this.disjointUnion.size() != f.getDisjointUnions().size())
            return false;
        if(this.disjointClasses.size() != f.getDisjointClasses().size())
            return false;

        if(this.superclassSpecificity != f.getSuperClassSpecificity())
            return false;
        if(this.equivalenceSpecificity != f.getEquivalenceSpecificity())
            return false;
        if(this.disjointUnionSpecificity != f.getDisjointUnionSpecificity())
            return false;
        if(this.disjointClassesSpecificity != f.getDisjointClassesSpecificity())
            return false; 

        if(!SetIRIGeneralisation.exists(this.superclasses, f.getSuperClasses()))
            return false;
        if(!SetIRIGeneralisation.exists(this.equivalences, f.getEquivalences()))
            return false;
        if(!SetIRIGeneralisation.exists(this.disjointClasses, f.getDisjointClasses()))
            return false;
        if(!SetIRIGeneralisation.exists(this.disjointUnion, f.getDisjointUnions()))
            return false;

        Set<SyntaxTree> allHere = new HashSet<>();
        allHere.addAll(this.superclasses);
        allHere.addAll(this.equivalences);
        allHere.addAll(this.disjointUnion);
        allHere.addAll(this.disjointClasses);

        Set<SyntaxTree> allThere = new HashSet<>();
        allThere.addAll(f.getSuperClasses());
        allThere.addAll(f.getEquivalences());
        allThere.addAll(f.getDisjointUnions());
        allThere.addAll(f.getDisjointClasses());

        return SetRenaming.exists(allHere,allThere); 
        //return SetRenamingEdges.exists(allHere,allThere);
    } 

    private int getSpecificity(Set<SyntaxTree> ts){
        int res = 0;
        for(SyntaxTree t : ts){
            res += getSpecificity(t);
        }
        return res;
    } 

    private int getSpecificity(SyntaxTree t){
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();
        return graph.vertexSet().size(); 
    } 
}
