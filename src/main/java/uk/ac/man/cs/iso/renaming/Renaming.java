package uk.ac.man.cs.iso.renaming;

import uk.ac.man.cs.iso.*;
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

public class Renaming {

    private OWLAxiom a1;
    private OWLAxiom a2;

    private Graph<SyntaxNode,DefaultEdge> g1;
    private Graph<SyntaxNode,DefaultEdge> g2;

    private Map<SyntaxNode,IRI> node2iri1;
    private Map<SyntaxNode,IRI> node2iri2;

    private boolean isRenaming;

    public static boolean exists(SyntaxTree s1, SyntaxTree s2) {
        Renaming r = new Renaming(s1,s2);
        return r.exists(); 
    }

    public Renaming(SyntaxTree t1, SyntaxTree t2) {
        this.g1 = t1.getTree();
        this.g2 = t2.getTree();
        this.a1 = ((AxiomNode) t1.getRoot()).getAxiom();
        this.a2 = ((AxiomNode) t2.getRoot()).getAxiom();
        this.initialise();
        this.isRenaming = this.exists(g1,g2); 
    }

    private void initialise(){
        this.node2iri1 = this.getIRInodes(this.g1);
        this.node2iri2 = this.getIRInodes(this.g2); 
    }

    public boolean exists(){
        return this.isRenaming;
    }

    private boolean exists(Graph<SyntaxNode,DefaultEdge> g1, Graph<SyntaxNode,DefaultEdge> g2) { 
        //SyntaxNodeComparator nodeCom = new SyntaxNodeComparator();
        SyntaxNodeComparatorRenaming nodeCom = new SyntaxNodeComparatorRenaming();
        DefaultEdgeComparator edgeCom = new DefaultEdgeComparator();

        VF2GraphIsomorphismInspector inspector = new VF2GraphIsomorphismInspector(g1,g2,nodeCom,edgeCom,true);

        boolean isomorphic = inspector.isomorphismExists();
        if(!isomorphic)
            return false; 

        OccurrsCheck check = new OccurrsCheck(g1,g2);

        if(check.noRepeatedIRIs()){
            return isomorphic;
        }

        if(!check.satisfiesSizeConstraints()){
            return false;
        }

        //TODO: check whether types of IRI match

        //BOTH isomorphic && repetitive entities
        //so we need to test for renaming
        Iterator<GraphMapping<SyntaxNode,DefaultEdge>> it = inspector.getMappings();
        while(it.hasNext()){//as long as there are mappings
            GraphMapping<SyntaxNode,DefaultEdge> mapping = it.next();
            //get mapping as map
            Map<IRI,IRI> validAssignment = getValidRenaming(mapping);
            if(validAssignment != null){//we only check whether a substitution is a bijection
                try{ //however, blind substitution of IRI's can 'break' things
                    if(testAssignment(validAssignment)){//meaning the substitution is not infact 'valid' in OWL
                        return true;
                    }
                } catch (Exception e){
                    ;//so ignore such substitutions
                }
            } 
        }
        return false;
    }

    private boolean testAssignment(Map<IRI,IRI> assignment) throws Exception {

        IRI IOR = IRI.create("http://owl.api.tutorial"); 
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager(); 
        OWLOntology o = manager.createOntology(IOR); 
        OWLDataFactory df = OWLManager.createOWLOntologyManager().getOWLDataFactory(); 
        o.add(this.a1);
        //OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

        OWLObjectTransformer<IRI> replacer = new OWLObjectTransformer<>((x) -> true, (input) -> {
            IRI l = assignment.get(input);
            if (l == null) {
                return input;
            }
            return l;
        } , df, IRI.class); 

        //List<AxiomChangeData> test = replacer.change(this.axiom);
        List<OWLOntologyChange> results = replacer.change(o);
        o.applyChanges(results);

        Set<OWLLogicalAxiom> axims =  o.getLogicalAxioms(true);
        for(OWLLogicalAxiom a : axims){
            if(a.equals(this.a2)){
                return true;
            }
        }

        return false;
    }

    private Map<IRI,IRI> getValidRenaming(GraphMapping<SyntaxNode,DefaultEdge> m){
        Map<IRI,IRI> forward = new HashMap<>();
        Map<IRI,IRI> backward = new HashMap<>();
        for(SyntaxNode n : this.node2iri1.keySet()){
            SyntaxNode corr = m.getVertexCorrespondence(n,true);
            IRI iri1 = node2iri1.get(n);
            IRI iri2 = node2iri2.get(corr);
            forward.putIfAbsent(iri1,iri2);
            backward.putIfAbsent(iri2,iri1);
            if(forward.containsKey(iri1)){
                if(!forward.get(iri1).equals(iri2))//not a valid mapping
                    return null; 
            } 
            if(backward.containsKey(iri2)){
                if(!backward.get(iri2).equals(iri1))
                    return null; 
            }
        }
        return forward; 
    } 

    private Map<SyntaxNode,IRI> getIRInodes(Graph<SyntaxNode,DefaultEdge> g){
        Map<SyntaxNode,IRI> map = new HashMap<>();
        Set<SyntaxNode> v = g.vertexSet();
        for(SyntaxNode n : v){
            if(n instanceof PropertyNode){ 
                if(!((PropertyNode) n).getPropertyExpression().isAnonymous()){ 
                    //IRI iri = ((PropertyNode) n).getPropertyExpression().getIRI(); 
                     Set<OWLEntity> ps = ((PropertyNode) n).getPropertyExpression().getSignature(); 
                     if(ps.size() > 1){
                         System.out.println("ERROR");
                     }
                     for(OWLEntity e : ps){ 
                         IRI iri = e.getIRI();
                         map.put(n,iri);
                     }
                }
            }
            if(n instanceof ClassNode){
                if(((ClassNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((ClassNode) n).getExpression().asOWLClass().getIRI(); 
                    map.put(n,iri);
                }
            }
            if(n instanceof IndividualNode){
                if(!((IndividualNode) n).getIndividual().isAnonymous()){ 
                    IRI iri = ((IndividualNode) n).getIndividual().asOWLNamedIndividual().getIRI(); 
                    map.put(n,iri);
                } 
            }

            if(n instanceof UnionNode){
                if(((UnionNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((UnionNode) n).getExpression().asOWLClass().getIRI(); 
                    map.put(n,iri);
                }
            }

            if(n instanceof SubClassOfNode){
                if(((SubClassOfNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((SubClassOfNode) n).getExpression().asOWLClass().getIRI(); 
                    map.put(n,iri);
                } 
            }

            if(n instanceof SuperClassOfNode){
                if(((SuperClassOfNode) n).getExpression().isOWLClass()){ 
                    IRI iri = ((SuperClassOfNode) n).getExpression().asOWLClass().getIRI(); 
                    map.put(n,iri);
                } 
            } 
        }
        return map; 
    } 
}
