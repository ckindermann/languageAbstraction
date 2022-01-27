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
public class SyntaxTreeBuilder extends TreeBuilder
    implements OWLAxiomVisitor,
               OWLClassExpressionVisitor,
               OWLPropertyExpressionVisitor,
               OWLIndividualVisitor,
               OWLDataRangeVisitor,
               OWLDataVisitor {
               //OWLLiteralVisitorBase {

    //implements OWLObjectVisitor (DEAR LOARD!)

    //We could just make this a simple graph eh?
    private SimpleDirectedGraph<SyntaxNode, DefaultEdge> syntaxTree;
    private SyntaxNode root;
    private SyntaxNode previousCall;

    //would have loved to have this ...
    //however some logical axioms, e.g.,
    //OWLAsymmetricObjectPropertyAxiom do not accept a logicalAxiomVisitor
    //(even though it has a 'visit' method.. just great)
    //private LogicalAxiomVisitor logicalAxiomVisitor;
    private AxiomVisitor axiomVisitor;

    public SyntaxTreeBuilder(){
        this.axiomVisitor = new AxiomVisitor();
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
        axiom.accept(this.axiomVisitor);
        SyntaxNode node = this.axiomVisitor.getSyntaxNode();//not exactly necessary.. only creates a new syntax node..
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
    public void visit(OWLDisjointClassesAxiom axiom){//TODO
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
    public void visit(OWLDisjointUnionAxiom axiom){//TODO
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
    public void visit(OWLEquivalentClassesAxiom axiom){//TODO
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
        //NB: OWL's structural specification says there can be n properties
        //however, the OWL API seems to support only n=1? 

        //create node for data property (leaf node)
        SyntaxNode parent = this.previousCall;
        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);

        //create node for data range (which has structure..)
        this.previousCall = parent; 
        OWLDataRange range = (OWLDataRange) ce.getFiller(); 
        this.addNode(range);
        range.accept(this);
    }
    public void visit(OWLDataExactCardinality ce){ 
        //integer
        int cardinality = ce.getCardinality();
        CardinalityNode cardNode = new CardinalityNode(cardinality);
        this.syntaxTree.addVertex(cardNode); 
        this.syntaxTree.addEdge(this.previousCall,cardNode); 

        SyntaxNode parent = this.previousCall;

        //DataPropertyExpression
        OWLDataPropertyExpression pe = ce.getProperty(); 
        this.addNode(pe);
        pe.accept(this);

        this.previousCall = parent;

        //Datarange
        OWLDataRange dataRange = ce.getFiller();
        if(dataRange != null){
            this.addNode(dataRange);
            dataRange.accept(this);
        }
    }
    public void visit(OWLDataHasValue ce){ 
        SyntaxNode parent = this.previousCall;
        //data property expression 
        OWLDataPropertyExpression pe = ce.getProperty();
        this.addNode(pe);
        pe.accept(this);

        this.previousCall = parent;

        //literal
        OWLLiteral filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLDataMaxCardinality ce){ 
        //integer
        int cardinality = ce.getCardinality();
        CardinalityNode cardNode = new CardinalityNode(cardinality);
        this.syntaxTree.addVertex(cardNode); 
        this.syntaxTree.addEdge(this.previousCall,cardNode); 

        SyntaxNode parent = this.previousCall;

        //DataPropertyExpression
        OWLDataPropertyExpression pe = ce.getProperty(); 
        this.addNode(pe);
        pe.accept(this);

        this.previousCall = parent;

        //Datarange
        OWLDataRange dataRange = ce.getFiller();
        if(dataRange != null){
            this.addNode(dataRange);
            dataRange.accept(this);
        }
    }
    public void visit(OWLDataMinCardinality ce){ 
        //integer
        int cardinality = ce.getCardinality();
        CardinalityNode cardNode = new CardinalityNode(cardinality);
        this.syntaxTree.addVertex(cardNode); 
        this.syntaxTree.addEdge(this.previousCall,cardNode); 

        SyntaxNode parent = this.previousCall;

        //DataPropertyExpression
        OWLDataPropertyExpression pe = ce.getProperty(); 
        this.addNode(pe);
        pe.accept(this);

        this.previousCall = parent;

        //Datarange
        OWLDataRange dataRange = ce.getFiller();
        if(dataRange != null){
            this.addNode(dataRange);
            dataRange.accept(this);
        }
    }
    public void visit(OWLDataSomeValuesFrom ce){ 
        SyntaxNode parent = this.previousCall;
        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);

        //create node for data range (which has structure..)
        this.previousCall = parent; 
        OWLDataRange range = (OWLDataRange) ce.getFiller(); 
        this.addNode(range);
        range.accept(this);
    }

    public void visit(OWLObjectOneOf ce){ 
        Set<OWLIndividual> individuals = ce.getIndividuals();
        SyntaxNode parent = this.previousCall;
        for(OWLIndividual i : individuals){
            this.previousCall = parent;
            this.addNode(i);
        }
        //there is no descension possible
        this.previousCall = null;
    }

    public void visit(OWLObjectHasSelf ce){ 
        OWLObjectPropertyExpression pe = ce.getProperty();
        this.addNode(pe);
        pe.accept(this);
    }

    public void visit(OWLObjectHasValue ce){ 
        OWLIndividual individual = (OWLIndividual) ce.getFiller();
        SyntaxNode parent = this.previousCall;
        this.addNode(individual);

        this.previousCall = parent; 
        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);
        property.accept(this); 
    }

    public void visit(OWLObjectAllValuesFrom ce){ 
        SyntaxNode parent = this.previousCall;

        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);
        property.accept(this);

        this.previousCall = parent;

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
        SyntaxNode parent = this.previousCall;

        //add child node for cardinality (which is a leaf - so no visiting/recrusion)
        int cardinality = ce.getCardinality();
        CardinalityNode cardNode = new CardinalityNode(cardinality);
        this.syntaxTree.addVertex(cardNode); 
        this.syntaxTree.addEdge(this.previousCall,cardNode); 

        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);
        property.accept(this);

        this.previousCall = parent;

        //recurse into substructure
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
        SyntaxNode parent = this.previousCall;

        int cardinality = ce.getCardinality();
        CardinalityNode cardNode = new CardinalityNode(cardinality);
        this.syntaxTree.addVertex(cardNode); 
        this.syntaxTree.addEdge(this.previousCall,cardNode); 

        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);
        property.accept(this);

        this.previousCall = parent;

        OWLClassExpression filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLObjectMinCardinality ce){
        SyntaxNode parent = this.previousCall;

        int cardinality = ce.getCardinality();
        CardinalityNode cardNode = new CardinalityNode(cardinality);
        this.syntaxTree.addVertex(cardNode); 
        this.syntaxTree.addEdge(this.previousCall,cardNode); 

        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);
        property.accept(this);

        this.previousCall = parent;

        OWLClassExpression filler = ce.getFiller();
        this.addNode(filler);
        filler.accept(this); 
    }
    public void visit(OWLObjectSomeValuesFrom ce){
        SyntaxNode parent = this.previousCall;
        OWLPropertyExpression property = ce.getProperty();
        this.addNode(property);
        property.accept(this);

        this.previousCall = parent;

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


    //===============================================================
    //============Property EXPRESSIONS VISITOR==========================
    //===============================================================
    private void addNode(OWLPropertyExpression expr){ 
        PropertyNode node = new PropertyNode(expr); 
        this.syntaxTree.addVertex(node);
        //add edges to graph
        if(this.previousCall != null){//this has to be true (assert this?)
            this.syntaxTree.addEdge(this.previousCall,node); 
        }
        this.previousCall = node; 
    }

    public void visit(OWLObjectInverseOf property){
        OWLObjectPropertyExpression pe = property.getInverse();
        this.addNode(pe);
        pe.accept(this); 
    }

    public void visit(OWLObjectProperty property){
        //we have already added a node for the class expression
        //now we recurse into its substructure 
        this.previousCall = null; //we don't create children from this call 
    }

    public void visit(OWLDataProperty property){
        this.previousCall = null;
    }

    //TODO
    public void visit(OWLAnnotationProperty property){
        ;
    }

    //===============================================================
    //============Individual VISITOR==========================
    //===============================================================
    private void addNode(OWLIndividual i){ 
        IndividualNode node = new IndividualNode(i); 
        this.syntaxTree.addVertex(node);
        //add edges to graph
        if(this.previousCall != null){//this has to be true (assert this?)
            this.syntaxTree.addEdge(this.previousCall,node); 
        }
        this.previousCall = node; 
    }

    public void visit(OWLAnonymousIndividual individual){
        this.previousCall = null; //we don't create children from this call 
    }
    public void visit(OWLNamedIndividual individual){
        this.previousCall = null; //we don't create children from this call 
    }

    //===============================================================
    //============Data Range VISITOR==========================
    //===============================================================
    private void addNode(OWLDataRange r){
        DataRangeNode node = new DataRangeNode(r);
        this.syntaxTree.addVertex(node);
        //add edges to graph
        if(this.previousCall != null){//this has to be true (assert this?)
            this.syntaxTree.addEdge(this.previousCall,node); 
        }
        this.previousCall = node; 
    }

    private void addNode(OWLLiteral l){
        LiteralNode node = new LiteralNode(l);
        this.syntaxTree.addVertex(node);
        //add edges to graph
        if(this.previousCall != null){//this has to be true (assert this?)
            this.syntaxTree.addEdge(this.previousCall,node); 
        }
        this.previousCall = node; 
    }

    private void addNode(OWLFacetRestriction r){
        FacetRestrictionNode node = new FacetRestrictionNode(r);
        this.syntaxTree.addVertex(node);
        //add edges to graph
        if(this.previousCall != null){//this has to be true (assert this?)
            this.syntaxTree.addEdge(this.previousCall,node); 
        }
        this.previousCall = node; 
    }

    public void visit(OWLDataComplementOf node){
        OWLDataRange r = node.getDataRange();
        this.addNode(r);
        r.accept(this);
    }
    public void visit(OWLDataIntersectionOf node){
        Set<OWLDataRange> rs = node.getOperands();
        SyntaxNode parent = this.previousCall;
        for(OWLDataRange r : rs){
            this.previousCall = parent;
            this.addNode(r);
            r.accept(this); 
        } 
    }
    public void visit(OWLDataOneOf node){
        //points to a set of literals
        Set<OWLLiteral> ls = node.getValues();
        SyntaxNode parent = this.previousCall;
        for(OWLLiteral l : ls){
            this.previousCall = parent;
            this.addNode(l);
            l.accept(this); //not necessary
        }

    }
    public void visit(OWLDatatypeRestriction node){
        SyntaxNode parent = this.previousCall;

        //data type
        OWLDatatype dt = node.getDatatype();
        this.addNode(dt);//is a datarange
        dt.accept(this);//unnecessary

        //N (facests+restriction value)
        //this.previousCall = parent;
        Set<OWLFacetRestriction> rs = node.getFacetRestrictions();
        for(OWLFacetRestriction r : rs){
            this.previousCall = parent;
            this.addNode(r);
            r.accept(this);
        } 
    }
    public void visit(OWLDataUnionOf node){
        Set<OWLDataRange> rs = node.getOperands();
        SyntaxNode parent = this.previousCall;
        for(OWLDataRange r : rs){
            this.previousCall = parent;
            this.addNode(r);
            r.accept(this); 
        } 
    } 

    public void visit(OWLDatatype node){
        this.previousCall = null;
    } 
    public void visit(OWLLiteral node){
        this.previousCall = null;
    }
    public void visit(OWLFacetRestriction node){
        //pull out facets? doesn't accept visitors - is not an OWLObject ...
        //OWLFacet facet = node.getFacet(); ... woul 
        OWLLiteral facetValue = node.getFacetValue();
        this.addNode(facetValue);
        facetValue.accept(this);
    }
}
