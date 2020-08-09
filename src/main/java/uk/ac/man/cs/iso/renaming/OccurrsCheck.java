package uk.ac.man.cs.iso.renaming;

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
import org.jgrapht.alg.isomorphism.*;

public class OccurrsCheck {

    private boolean multipleVars1;
    private boolean multipleVars2;
    private boolean matchingSizes;

    private TreeMap<Integer,Set<IRI>> stratificationByOccurrence1;
    private TreeMap<Integer,Set<IRI>> stratificationByOccurrence2;

    public OccurrsCheck(Graph<SyntaxNode,DefaultEdge> g1, Graph<SyntaxNode,DefaultEdge> g2){

        this.stratificationByOccurrence1 = getStratificationByOccurrence(g1);
        this.stratificationByOccurrence2 = getStratificationByOccurrence(g2);
        this.multipleVars1 = hasMultipleOccurrences(this.stratificationByOccurrence1);
        this.multipleVars2 = hasMultipleOccurrences(this.stratificationByOccurrence2);
        this.matchingSizes = sizeCheck(); 
    }

    public boolean noRepeatedIRIs(){
        if(!multipleVars1 && !multipleVars2)
            return true;
        return false;
    }

    public boolean satisfiesSizeConstraints(){
        return this.matchingSizes;
    }

    private boolean hasMultipleOccurrences(TreeMap<Integer,Set<IRI>> stratByOcc){
        for(Integer k : stratByOcc.keySet()){
            if(k > 1){//there exists an IRI with more than 2 ocurrences
                return true;
            }
        }
        return false;
    }

    private boolean sizeCheck(){
        if(!multipleVars1 || !multipleVars2){ 
            return false;
        }

        if(stratificationByOccurrence1.size() != stratificationByOccurrence2.size()){
            return false;
        }

        for(Map.Entry<Integer,Set<IRI>> entry : stratificationByOccurrence1.entrySet()){
            int occurrence = entry.getKey();
            int iris1 = entry.getValue().size();

            if(!this.stratificationByOccurrence2.containsKey(occurrence)){
                return false;
            } else {
                int iris2 = this.stratificationByOccurrence2.get(occurrence).size();
                if(iris1 != iris2){
                    return false;
                }
            }
        }
        return true; 
    }

    private TreeMap<Integer,Set<IRI>> getStratificationByOccurrence(Graph<SyntaxNode,DefaultEdge> g){
        Map<IRI,Integer> map = signatureOccurrence(g);
        TreeMap<Integer,Set<IRI>> occurrence2irisLocal = new TreeMap<>(); 
        for(Map.Entry<IRI,Integer> entry : map.entrySet()){
            IRI iri = entry.getKey();
            int occurrence = entry.getValue();
            occurrence2irisLocal.putIfAbsent(occurrence, new HashSet<>());
            occurrence2irisLocal.get(occurrence).add(iri); 
        }
        return occurrence2irisLocal; 
    }

    private Map<IRI,Integer> signatureOccurrence(Graph<SyntaxNode,DefaultEdge> g){
        Map<IRI,Integer> occurrences = new HashMap<>();
        for(SyntaxNode n : g.vertexSet()){
            if(n instanceof PropertyNode){ 
                if(!((PropertyNode) n).getPropertyExpression().isAnonymous()){ 
                    //IRI iri = ((PropertyNode) n).getPropertyExpression().getIRI(); 
                     Set<OWLEntity> ps = ((PropertyNode) n).getPropertyExpression().getSignature(); 
                     if(ps.size() > 1){
                         System.out.println("ERROR");
                     }
                     for(OWLEntity e : ps){ 
                         IRI iri = e.getIRI();
                         occurrences.putIfAbsent(iri,0);
                         int update = occurrences.get(iri) +1;
                         occurrences.replace(iri,update);
                     }
                }
            }
            if(n instanceof ClassNode){
                if(((ClassNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((ClassNode) n).getExpression().asOWLClass().getIRI(); 
                    occurrences.putIfAbsent(iri,0);
                    int update = occurrences.get(iri) +1;
                    occurrences.replace(iri,update);
                }
            }
            if(n instanceof IndividualNode){
                if(!((IndividualNode) n).getIndividual().isAnonymous()){ 
                    IRI iri = ((IndividualNode) n).getIndividual().asOWLNamedIndividual().getIRI(); 
                    occurrences.putIfAbsent(iri,0);
                    int update = occurrences.get(iri) +1;
                    occurrences.replace(iri,update);
                } 
            }

            if(n instanceof UnionNode){
                if(((UnionNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((UnionNode) n).getExpression().asOWLClass().getIRI(); 
                    occurrences.putIfAbsent(iri,0);
                    int update = occurrences.get(iri) +1;
                    occurrences.replace(iri,update);
                }
            }

            if(n instanceof SubClassOfNode){
                if(((SubClassOfNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((SubClassOfNode) n).getExpression().asOWLClass().getIRI(); 
                    occurrences.putIfAbsent(iri,0);
                    int update = occurrences.get(iri) +1;
                    occurrences.replace(iri,update);
                } 
            }

            if(n instanceof SuperClassOfNode){
                if(((SuperClassOfNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((SuperClassOfNode) n).getExpression().asOWLClass().getIRI(); 
                    occurrences.putIfAbsent(iri,0);
                    int update = occurrences.get(iri) +1;
                    occurrences.replace(iri,update);
                } 
            } 
        }
        return occurrences; 
    } 
}
