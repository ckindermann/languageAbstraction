package uk.ac.man.cs.serialiser;

import uk.ac.man.cs.util.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;

import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.*;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.util.*;

public class InternalTreeStructure {

    public static String getClassExpressionSymbol(OWLClassExpression expr) {
        if (expr instanceof OWLClass) {
            return expr.toString();
        }
        if (expr instanceof OWLDataExactCardinality) {
            return "DEC";
        }
        if (expr instanceof OWLDataAllValuesFrom) {
            return "DAV";
        }
        if (expr instanceof OWLDataSomeValuesFrom) {
            return "DSV";
        }
        if (expr instanceof OWLDataHasValue) {
            return "DHV";
        }
        if (expr instanceof OWLDataMaxCardinality) {
            return "DMaxC";
        }
        if (expr instanceof OWLDataMinCardinality) {
            return "DMinC";
        }
        if (expr instanceof OWLObjectOneOf) {
            return "OOO";
        }
        if (expr instanceof OWLObjectHasSelf) {
            return "OHS";
        }
        if (expr instanceof OWLObjectHasValue) {
            return "OHV";
        }
        if (expr instanceof OWLObjectAllValuesFrom) {
            return "OAV";
        }
        if (expr instanceof OWLObjectComplementOf) {
            return "OC";
        }
        if (expr instanceof OWLObjectExactCardinality) {
            return "OEC";
        }
        if (expr instanceof OWLObjectIntersectionOf) {
            return "OI";
        }
        if (expr instanceof OWLObjectMaxCardinality) {
            return "OMaxC";
        }
        if (expr instanceof OWLObjectMinCardinality) {
            return "OMinC";
        }
        if (expr instanceof OWLObjectSomeValuesFrom) {
            return "OSV";
        }
        if (expr instanceof OWLObjectUnionOf) {
            return "OU";
        }
        return null;

    }

    public static String getNodeSymbol(SyntaxNode n) {

        //Axiom Node
        if(n instanceof AxiomNode){
            AxiomNode node = (AxiomNode) n;
            OWLAxiom axiom = node.getAxiom();
            //TODO: implement a visitor here
            if (axiom instanceof OWLSubClassOfAxiom){
                return "SC";
            }
            if (axiom instanceof OWLEquivalentClassesAxiom){
                return "EC";
            }
            if (axiom instanceof OWLDisjointClassesAxiom){
                return "DC";
            }
            if (axiom instanceof OWLDisjointUnionAxiom){
                return "DU";
            }
        }

        if (n instanceof ClassNode) {
            ClassNode node = (ClassNode) n;
            OWLClassExpression expr = node.getExpression(); 
            return getClassExpressionSymbol(expr);
        }

        if (n instanceof UnionNode) {
            UnionNode node = (UnionNode) n;
            OWLClassExpression expr = node.getExpression(); 
            return getClassExpressionSymbol(expr);
        }

        if (n instanceof SubClassOfNode) {
            SubClassOfNode node = (SubClassOfNode) n;
            OWLClassExpression expr = node.getExpression(); 
            return getClassExpressionSymbol(expr);
        }

        if (n instanceof SuperClassOfNode) {
            SuperClassOfNode node = (SuperClassOfNode) n;
            OWLClassExpression expr = node.getExpression(); 
            return getClassExpressionSymbol(expr);
        }

        if (n instanceof CardinalityNode) {
            CardinalityNode node = (CardinalityNode) n;
            int cardinality = node.getCardinality();
            return "" + cardinality; 
        }
        
        if (n instanceof DataRangeNode) {
            DataRangeNode node = (DataRangeNode) n;
            OWLDataRange range = node.getDataRange();

            if(range instanceof OWLDataComplementOf){
                return "DComp";
            }
            if(range instanceof OWLDataIntersectionOf){
                return "DI";
            }
            if(range instanceof OWLDataUnionOf){
                return "DUO";
            }
            if(range instanceof OWLDataOneOf){
                return "DOO";
            }
            if(range instanceof OWLDatatype){
                return range.toString();
            }
            if(range instanceof OWLDatatypeRestriction){
                return "DTR";
            } 
        }

        if (n instanceof FacetRestrictionNode) {
            FacetRestrictionNode node = (FacetRestrictionNode) n;
            return node.getFacetRestriction().toString(); 
        }

        if (n instanceof IndividualNode) {
            IndividualNode node = (IndividualNode) n;
            return node.getIndividual().toString(); 
        }

        if (n instanceof LiteralNode) {
            LiteralNode node = (LiteralNode) n;
            return node.getLiteral().toString(); 
        }

        if (n instanceof PropertyNode) {
            PropertyNode node = (PropertyNode) n; 
            OWLPropertyExpression expr = node.getPropertyExpression();
            if (expr instanceof OWLObjectInverseOf){
                return "inv";
            }

            if(expr.isObjectPropertyExpression()){ 
                OWLObjectPropertyExpression expr_2 = (OWLObjectPropertyExpression) expr;
                return expr_2.getNamedProperty().toString();
            } 

            if(expr.isDataPropertyExpression()){ 
                OWLDataPropertyExpression expr_2 = (OWLDataPropertyExpression) expr;
                return expr_2.toString();
            } 

        }


        return "asd";
    }

    public static String getEdgeLabel(SyntaxNode source, SyntaxNode target){
        String label = "*";

        if(source instanceof AxiomNode && target instanceof SubClassOfNode){
            return "SubClass";
        }

        if(source instanceof AxiomNode && target instanceof SuperClassOfNode){
            return "SuperClass";
        }

        if(source instanceof AxiomNode && target instanceof UnionNode){
            return "Union";
        } 

        if(target instanceof CardinalityNode){
            return "Cardinality";
        } 

        if(target instanceof IndividualNode){
            return "Individual";
        } 

        if(target instanceof LiteralNode){
            return "Literal";
        } 

        if(target instanceof PropertyNode){
            return "Property";
        } 

        //SubClass,Superclass,Classnode -> SubClass, SuperClass, ClassNode


        return label;

    }

    public static void serialise(SyntaxTree t) {
        List<String> nodes = new ArrayList<>();
        List<String> edges = new ArrayList<>();


        int nodeId = 1;
        Map<SyntaxNode,Integer> node2id = new HashMap<>();
        Map<Integer,SyntaxNode> id2node = new TreeMap<>();

        SimpleDirectedGraph<SyntaxNode, DefaultEdge> syntaxTree = t.getTree();

        //start node
        SyntaxNode root = t.getRoot(); 


        Set<DefaultEdge> currentLevel = new HashSet<>();
        Set<DefaultEdge> nextLevel = new HashSet<>();

        nextLevel.addAll(syntaxTree.outgoingEdgesOf(root));

        while(!nextLevel.isEmpty()){
            currentLevel.addAll(nextLevel);
            nextLevel.clear(); 
            for (DefaultEdge e : currentLevel){

                SyntaxNode source = syntaxTree.getEdgeSource(e);
                SyntaxNode target = syntaxTree.getEdgeTarget(e);
                nextLevel.addAll(syntaxTree.outgoingEdgesOf(target));

                if(!node2id.containsKey(source)){
                    node2id.put(source,nodeId);
                    id2node.put(nodeId,source);
                    nodeId++; 
                }

                if(!node2id.containsKey(target)){
                    node2id.put(target,nodeId);
                    id2node.put(nodeId,target);
                    nodeId++; 
                } 

                int source_id = node2id.get(source);
                int target_id = node2id.get(target);
                String label = getEdgeLabel(source,target); 
                edges.add(source_id + "," + target_id + "," + label);
            }
            currentLevel.clear();
        }


        for (Map.Entry<Integer, SyntaxNode> entry : id2node.entrySet()) {

            // Printing all elements of a Map
            System.out.println(entry.getKey() + "," + getNodeSymbol(entry.getValue()));
        }
        System.out.println("Edges");
        for (String e : edges){
            System.out.println(e);
        }

    }

}

