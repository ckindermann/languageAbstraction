package uk.ac.man.cs.parser;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;

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

/**
 * Class Expression Visitor 
 */
public class CEPreservanceBuilder implements OWLAxiomVisitor,
               OWLClassExpressionVisitor {

    private SimpleDirectedGraph<SyntaxNode, DefaultEdge> syntaxTree;
    private SyntaxNode root;
    private SyntaxNode previousCall;

    public CEPreservanceBuilder(){ 
        ;
    }

    public SyntaxTree build(OWLAxiom axiom){
        this.syntaxTree = new SimpleDirectedGraph<>(DefaultEdge.class);
        axiom.accept(this);
        return new SyntaxTree(this.syntaxTree, this.root); 
    }

    public SyntaxTree build(OWLClassExpression ce){
        this.syntaxTree = new SimpleDirectedGraph<>(DefaultEdge.class);
        this.parseRoot(ce); 
        return new SyntaxTree(this.syntaxTree, this.root);
    }

//===============================================================
//===================AXIOM VISITOR===========================
//===============================================================

    private void parseRoot(OWLAxiom axiom){ 
        SyntaxNode node = new AxiomNode(axiom);
        this.root = node;
        this.syntaxTree.addVertex(node);
        this.previousCall = node; 
    }

    public void visit(OWLAsymmetricObjectPropertyAxiom axiom){
        this.parseRoot(axiom);
        //recurse down the structure (has properties)
    }
    public void visit(OWLClassAssertionAxiom axiom){
        this.parseRoot(axiom); 
    }
    public void visit(OWLDataPropertyAssertionAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLDataPropertyDomainAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLDataPropertyRangeAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLDifferentIndividualsAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLDisjointClassesAxiom axiom){
        this.parseRoot(axiom);

        Stream<OWLClassExpression> ops = axiom.operands();
        Set<OWLClassExpression> classes = ops.collect(Collectors.toSet()); 
        SyntaxNode parent = this.previousCall;
        for(OWLClassExpression c : classes){
            this.previousCall = parent;//set correct parent node
            this.addNode(c);
            //ClassNode node = new ClassNode(c); //create node
            //this.syntaxTree.addVertex(node); //add to syntax tree
            //this.syntaxTree.addEdge(this.previousCall,node);//connect node to root
            c.accept(this);//recurse to subexpressions 
        }
    }
    public void visit(OWLDisjointDataPropertiesAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLDisjointObjectPropertiesAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLDisjointUnionAxiom axiom){
        this.parseRoot(axiom);

        SyntaxNode parent = this.previousCall;
        OWLClassExpression union = axiom.getOWLClass();

        UnionNode node = new UnionNode(union); 
        //this.addNode(union);
        this.syntaxTree.addVertex(node);
        this.syntaxTree.addEdge(this.previousCall,node); 
        this.previousCall = node; 
        union.accept(this);

        this.previousCall = parent;
        Stream<OWLClassExpression> ops = axiom.operands();
        Set<OWLClassExpression> operands = ops.collect(Collectors.toSet()); 

        for(OWLClassExpression o : operands){
            this.previousCall = parent;
            this.addNode(o);
            o.accept(this); 
        }

    }
    public void visit(OWLEquivalentClassesAxiom axiom){
        this.parseRoot(axiom);
        Stream<OWLClassExpression> ops = axiom.operands();
        Set<OWLClassExpression> classes = ops.collect(Collectors.toSet());

        SyntaxNode parent = this.previousCall;
        for(OWLClassExpression c : classes){
            this.previousCall = parent;//set correct parent node

            this.addNode(c);
            //ClassNode node = new ClassNode(c); //create node 
            //this.syntaxTree.addVertex(node); //add to syntax tree
            //this.syntaxTree.addEdge(this.previousCall,node);//connect node to root

            c.accept(this);//recurse to subexpressions 
        }
    }
    public void visit(OWLEquivalentDataPropertiesAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLFunctionalDataPropertyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLFunctionalObjectPropertyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLHasKeyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLInverseObjectPropertiesAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLObjectPropertyAssertionAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLObjectPropertyDomainAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLObjectPropertyRangeAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLReflexiveObjectPropertyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLSameIndividualAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLSubClassOfAxiom axiom){
        this.parseRoot(axiom);
        SyntaxNode parent = this.previousCall;

        OWLClassExpression subClass = axiom.getSubClass();
        SubClassOfNode subnode = new SubClassOfNode(subClass); 
        this.syntaxTree.addVertex(subnode);
        this.syntaxTree.addEdge(this.previousCall,subnode); 
        this.previousCall = subnode; 
        subClass.accept(this);

        this.previousCall = parent;
        OWLClassExpression superClass = axiom.getSuperClass();
        //this.addNode(superClass);
        SuperClassOfNode node = new SuperClassOfNode(superClass); 
        this.syntaxTree.addVertex(node);
        this.syntaxTree.addEdge(this.previousCall,node); 
        this.previousCall = node; 
        superClass.accept(this); 
    }
    public void visit(OWLSubDataPropertyOfAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLSubObjectPropertyOfAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLSubPropertyChainOfAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLSymmetricObjectPropertyAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        this.parseRoot(axiom);
    }


    public void visit(SWRLRule node){
        System.out.println("Parsed a SWRLRule - cannot be handled");
    }
    public void doDefault(Object object){
        System.out.println("Parsed an Object in 'doDefault' - cannot be handled"); 
    }
    public void getDefaultReturnValue(Object object){
        System.out.println("Parsed an Object in 'getDefaultReturnValue' - cannot be handled"); 
    }
    public void handleDefault(Object c){
        System.out.println("Parsed an Object in 'handleDefault' - cannot be handled"); 
    }
    //-----------------
    public void visit(OWLAnnotationAssertionAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLAnnotationPropertyDomainAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLAnnotationPropertyRangeAxiom axiom){
        this.parseRoot(axiom);
    }
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom){
        this.parseRoot(axiom);
    }
//===============================================================
//============CLASS EXPRESSIONS VISITOR==========================
//===============================================================
//
    private void parseRoot(OWLClassExpression ce){
        SyntaxNode node = new ClassNode(ce);
        this.root = node;
        this.syntaxTree.addVertex(node);
        this.previousCall = node; 
        ce.accept(this);
    }

    private void addNode(OWLClassExpression expr){ 
        ClassNode node = new ClassNode(expr); 
        this.syntaxTree.addVertex(node);
        //add edges to graph
        if(this.previousCall != null){//this has to be true (assert this?)
            this.syntaxTree.addEdge(this.previousCall,node); 
        }
        this.previousCall = node; 
    }

    public void visit(OWLClass ce){ 
        //we have already added a node for the class expression
        //now we recurse into its substructure 
        this.previousCall = null; //we don't create children from this call
    }

    public void visit(OWLDataAllValuesFrom ce){ 
        this.previousCall = null;
    }
    public void visit(OWLDataExactCardinality ce){ 
        this.previousCall = null;
    }
    public void visit(OWLDataHasValue ce){
        this.previousCall = null;
    }
    public void visit(OWLDataMaxCardinality ce){
        this.previousCall = null;
    }
    public void visit(OWLDataMinCardinality ce){
        this.previousCall = null;
    }
    public void visit(OWLDataSomeValuesFrom ce){
        this.previousCall = null;
    }

    public void visit(OWLObjectOneOf ce){
        this.previousCall = null;
    }

    public void visit(OWLObjectHasSelf ce){
        this.previousCall = null;
    }

    public void visit(OWLObjectHasValue ce){
        this.previousCall = null;
    }

    public void visit(OWLObjectAllValuesFrom ce){ 
        OWLClassExpression filler = ce.getFiller();//subexpression
        this.addNode(filler);//set up tree structure
        filler.accept(this); //descend to subexpression
    }
    public void visit(OWLObjectComplementOf ce){ 
        OWLClassExpression complement = ce.getOperand();
        this.addNode(complement); 
        complement.accept(this);
    }
    public void visit(OWLObjectExactCardinality ce){ 
        OWLClassExpression filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLObjectIntersectionOf ce){
        Set<OWLClassExpression> operands = ce.getOperands();
        SyntaxNode parent = this.previousCall;

        for(OWLClassExpression o : operands){
            this.previousCall = parent;
            this.addNode(o);
            o.accept(this);
        }
    }
    public void visit(OWLObjectMaxCardinality ce){ 
        OWLClassExpression filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLObjectMinCardinality ce){
        OWLClassExpression filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLObjectSomeValuesFrom ce){
        OWLClassExpression filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLObjectUnionOf ce){
        Set<OWLClassExpression> operands = ce.getOperands();
        SyntaxNode parent = this.previousCall;
        for(OWLClassExpression o : operands){
            this.previousCall = parent;
            this.addNode(o);
            o.accept(this);
        }
    } 
}
