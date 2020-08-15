package uk.ac.man.cs.iso.gg;

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


/**
 * Syntax Node
 * A syntax node 
 */
public class SyntaxNodeComparator implements Comparator<SyntaxNode> {

    public SyntaxNodeComparator(){
        ;
    }

    @Override
    public int compare(SyntaxNode n1, SyntaxNode n2){

        if(n1 instanceof AxiomNode && n2 instanceof AxiomNode)
            return compareAxiomNodes((AxiomNode) n1, (AxiomNode) n2);

        if(n1 instanceof ClassNode && n2 instanceof ClassNode)
            return compareClassNode(((ClassNode) n1), ((ClassNode) n2));

        if(n1 instanceof SubClassOfNode && n2 instanceof SubClassOfNode)
            return compareSubClassOfNode(((SubClassOfNode) n1), ((SubClassOfNode) n2));

        if(n1 instanceof SuperClassOfNode && n2 instanceof SuperClassOfNode)
            return compareSuperClassOfNode(((SuperClassOfNode) n1), ((SuperClassOfNode) n2));

        if(n1 instanceof UnionNode && n2 instanceof UnionNode)
            return compareUnionNode(((UnionNode) n1), ((UnionNode) n2));

        if(n1 instanceof CardinalityNode && n2 instanceof CardinalityNode)
            return compareCardinalityNode((CardinalityNode) n1, (CardinalityNode) n2);

        if(n1 instanceof DataRangeNode && n2 instanceof DataRangeNode){
            return compareDataRangeNode((DataRangeNode) n1, (DataRangeNode) n2);
        }

        if(n1 instanceof FacetRestrictionNode && n2 instanceof FacetRestrictionNode){
            return compareFacetRestrictionNode((FacetRestrictionNode) n1, (FacetRestrictionNode) n2);
        }
        if(n1 instanceof IndividualNode && n2 instanceof IndividualNode){
            return compareIndividualNodes((IndividualNode) n1, (IndividualNode) n2);
        }
        if(n1 instanceof LiteralNode && n2 instanceof LiteralNode){
            return compareLiteralNode((LiteralNode) n1, (LiteralNode) n2);
        }
        if(n1 instanceof PropertyNode && n2 instanceof PropertyNode){
            return comparePropertyNode((PropertyNode) n1, (PropertyNode) n2);
        }

        //nodes are not ordered
        return -1;
    }

    private int comparePropertyNode(PropertyNode n1, PropertyNode n2){ 
        if(n1.getPropertyExpression().getClass() == n2.getPropertyExpression().getClass())
            return 0;
        return -1;
    }


    private int compareLiteralNode(LiteralNode n1, LiteralNode n2){ 
        if(n1.getLiteral().getClass() == n2.getLiteral().getClass())//TODO: print this
            return 0;
        return -1;
    }

    private int compareIndividualNodes(IndividualNode n1, IndividualNode n2){
        if(n1.getIndividual().getClass() == n2.getIndividual().getClass())
            return 0;
        return -1;
    }

    private int compareFacetRestrictionNode(FacetRestrictionNode n1, FacetRestrictionNode n2){
        if(n1.getFacetRestriction().getFacet().getClass() == n2.getFacetRestriction().getFacet().getClass())//TODO: print this:
            return 0;
        return -1; 
    }

    private int compareDataRangeNode(DataRangeNode n1, DataRangeNode n2){
        if(n1.getDataRange().getClass() == n2.getDataRange().getClass())//TODO: print this!
            return 0;
        return -1; 
    }

    private int compareAxiomNodes(AxiomNode n1, AxiomNode n2){
        //TODO: list everything for fine grained comparison
        //
        //as for now: cheat
        if(n1.getAxiom().getClass() == n2.getAxiom().getClass())
            return 0;
        return -1;
    }

    private int compareCardinalityNode(CardinalityNode n1, CardinalityNode n2){
        //if(n1.getCardinality() == n2.getCardinality())
            return 0;//all numbers are generalised over
        //return -1; 
    }

    //TODO: yeah yeah yeah, repetitive code - its a prototype
    private int compareUnionNode(UnionNode n1, UnionNode n2){
        //compare expression types
        if((n1.getExpression() instanceof OWLClass)  && (n2.getExpression() instanceof OWLClass))
            return 0;
        if((n1.getExpression() instanceof OWLDataAllValuesFrom)  && (n2.getExpression() instanceof OWLDataAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLDataExactCardinality)  && (n2.getExpression() instanceof OWLDataExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataHasValue)  && (n2.getExpression() instanceof OWLDataHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLDataMaxCardinality)  && (n2.getExpression() instanceof OWLDataMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataMinCardinality)  && (n2.getExpression() instanceof OWLDataMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataSomeValuesFrom)  && (n2.getExpression() instanceof OWLDataSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectOneOf)  && (n2.getExpression() instanceof OWLObjectOneOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasSelf)  && (n2.getExpression() instanceof OWLObjectHasSelf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasValue)  && (n2.getExpression() instanceof OWLObjectHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLObjectAllValuesFrom)  && (n2.getExpression() instanceof OWLObjectAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectComplementOf)  && (n2.getExpression() instanceof OWLObjectComplementOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectExactCardinality)  && (n2.getExpression() instanceof OWLObjectExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectIntersectionOf)  && (n2.getExpression() instanceof OWLObjectIntersectionOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMaxCardinality)  && (n2.getExpression() instanceof OWLObjectMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMinCardinality)  && (n2.getExpression() instanceof OWLObjectMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectSomeValuesFrom)  && (n2.getExpression() instanceof OWLObjectSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectUnionOf)  && (n2.getExpression() instanceof  OWLObjectUnionOf))
            return 0;
        return -1;

    }




    private int compareSuperClassOfNode(SuperClassOfNode n1, SuperClassOfNode n2){
        //compare expression types
        if((n1.getExpression() instanceof OWLClass)  && (n2.getExpression() instanceof OWLClass))
            return 0;
        if((n1.getExpression() instanceof OWLDataAllValuesFrom)  && (n2.getExpression() instanceof OWLDataAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLDataExactCardinality)  && (n2.getExpression() instanceof OWLDataExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataHasValue)  && (n2.getExpression() instanceof OWLDataHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLDataMaxCardinality)  && (n2.getExpression() instanceof OWLDataMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataMinCardinality)  && (n2.getExpression() instanceof OWLDataMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataSomeValuesFrom)  && (n2.getExpression() instanceof OWLDataSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectOneOf)  && (n2.getExpression() instanceof OWLObjectOneOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasSelf)  && (n2.getExpression() instanceof OWLObjectHasSelf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasValue)  && (n2.getExpression() instanceof OWLObjectHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLObjectAllValuesFrom)  && (n2.getExpression() instanceof OWLObjectAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectComplementOf)  && (n2.getExpression() instanceof OWLObjectComplementOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectExactCardinality)  && (n2.getExpression() instanceof OWLObjectExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectIntersectionOf)  && (n2.getExpression() instanceof OWLObjectIntersectionOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMaxCardinality)  && (n2.getExpression() instanceof OWLObjectMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMinCardinality)  && (n2.getExpression() instanceof OWLObjectMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectSomeValuesFrom)  && (n2.getExpression() instanceof OWLObjectSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectUnionOf)  && (n2.getExpression() instanceof  OWLObjectUnionOf))
            return 0;
        return -1;

    }



    private int compareSubClassOfNode(SubClassOfNode n1, SubClassOfNode n2){
        //compare expression types
        if((n1.getExpression() instanceof OWLClass)  && (n2.getExpression() instanceof OWLClass))
            return 0;
        if((n1.getExpression() instanceof OWLDataAllValuesFrom)  && (n2.getExpression() instanceof OWLDataAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLDataExactCardinality)  && (n2.getExpression() instanceof OWLDataExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataHasValue)  && (n2.getExpression() instanceof OWLDataHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLDataMaxCardinality)  && (n2.getExpression() instanceof OWLDataMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataMinCardinality)  && (n2.getExpression() instanceof OWLDataMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataSomeValuesFrom)  && (n2.getExpression() instanceof OWLDataSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectOneOf)  && (n2.getExpression() instanceof OWLObjectOneOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasSelf)  && (n2.getExpression() instanceof OWLObjectHasSelf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasValue)  && (n2.getExpression() instanceof OWLObjectHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLObjectAllValuesFrom)  && (n2.getExpression() instanceof OWLObjectAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectComplementOf)  && (n2.getExpression() instanceof OWLObjectComplementOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectExactCardinality)  && (n2.getExpression() instanceof OWLObjectExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectIntersectionOf)  && (n2.getExpression() instanceof OWLObjectIntersectionOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMaxCardinality)  && (n2.getExpression() instanceof OWLObjectMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMinCardinality)  && (n2.getExpression() instanceof OWLObjectMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectSomeValuesFrom)  && (n2.getExpression() instanceof OWLObjectSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectUnionOf)  && (n2.getExpression() instanceof  OWLObjectUnionOf))
            return 0;
        return -1;

    }


    private int compareClassNode(ClassNode n1, ClassNode n2){
        //compare expression types
        if((n1.getExpression() instanceof OWLClass)  && (n2.getExpression() instanceof OWLClass))
            return 0;
        if((n1.getExpression() instanceof OWLDataAllValuesFrom)  && (n2.getExpression() instanceof OWLDataAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLDataExactCardinality)  && (n2.getExpression() instanceof OWLDataExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataHasValue)  && (n2.getExpression() instanceof OWLDataHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLDataMaxCardinality)  && (n2.getExpression() instanceof OWLDataMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataMinCardinality)  && (n2.getExpression() instanceof OWLDataMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLDataSomeValuesFrom)  && (n2.getExpression() instanceof OWLDataSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectOneOf)  && (n2.getExpression() instanceof OWLObjectOneOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasSelf)  && (n2.getExpression() instanceof OWLObjectHasSelf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectHasValue)  && (n2.getExpression() instanceof OWLObjectHasValue))
            return 0;
        if((n1.getExpression() instanceof OWLObjectAllValuesFrom)  && (n2.getExpression() instanceof OWLObjectAllValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectComplementOf)  && (n2.getExpression() instanceof OWLObjectComplementOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectExactCardinality)  && (n2.getExpression() instanceof OWLObjectExactCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectIntersectionOf)  && (n2.getExpression() instanceof OWLObjectIntersectionOf))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMaxCardinality)  && (n2.getExpression() instanceof OWLObjectMaxCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectMinCardinality)  && (n2.getExpression() instanceof OWLObjectMinCardinality))
            return 0;
        if((n1.getExpression() instanceof OWLObjectSomeValuesFrom)  && (n2.getExpression() instanceof OWLObjectSomeValuesFrom))
            return 0;
        if((n1.getExpression() instanceof OWLObjectUnionOf)  && (n2.getExpression() instanceof  OWLObjectUnionOf))
            return 0;
        return -1;
    }

    @Override
    public boolean equals(Object obj){
        return this == obj;
    } 
}
