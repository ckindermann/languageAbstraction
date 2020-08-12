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

public class SetRenamingEdges {

    private Set<OWLAxiom> a1;
    private Set<OWLAxiom> a2;

    private Graph<SyntaxNode,RenamingEdge> g1;
    private Graph<SyntaxNode,RenamingEdge> g2;

    private Graph<SyntaxNode,DefaultEdge> o1;
    private Graph<SyntaxNode,DefaultEdge> o2;

    private Map<SyntaxNode,IRI> node2iri1;
    private Map<SyntaxNode,IRI> node2iri2;

    private Map<IRI,Set<SyntaxNode>> iri2nodes1;
    private Map<IRI,Set<SyntaxNode>> iri2nodes2;

    private Map<IRI,Integer> iri2occurrence1;
    private Map<IRI,Integer> iri2occurrence2;

    private boolean isRenaming;

    public static boolean exists(Set<SyntaxTree> s1, Set<SyntaxTree> s2) {
        SymmetricDifferenceEdgesRenaming sdr = new SymmetricDifferenceEdgesRenaming(s1,s2);
        if(sdr.exists()){
            return true;
        }

        SetRenamingEdges r = new SetRenamingEdges(s1,s2);
        return r.exists(); 
    }

    public SetRenamingEdges(Set<SyntaxTree> t1, Set<SyntaxTree> t2) {

        this.a1 = new HashSet<>();
        this.a2 = new HashSet<>();

        this.o1 = new SimpleDirectedGraph<>(DefaultEdge.class);
        this.o2 = new SimpleDirectedGraph<>(DefaultEdge.class);


        for(SyntaxTree t : t1){ 
            Graphs.addGraph(this.o1,t.getTree());
            this.a1.add(((AxiomNode) t.getRoot()).getAxiom());
        }
        for(SyntaxTree t : t2){ 
            Graphs.addGraph(this.o2,t.getTree());
            this.a2.add(((AxiomNode) t.getRoot()).getAxiom());
        } 

        //ATTENTION: non directed anymore! (need that for renaming constraints)
        this.g1 = new SimpleGraph<>(RenamingEdge.class);
        this.g2 = new SimpleGraph<>(RenamingEdge.class);

        //TODO: build new tree with different Edges
        //1. copy o1 with new 'renaming edges'
        //2. add renaming constraint edges

        //1.
        //1.1. copy vertex set
        Graphs.addAllVertices(this.g1,this.o1.vertexSet());
        Graphs.addAllVertices(this.g2,this.o2.vertexSet());
        //1.2. copy edge set
        //Graphs.addAllEdges(this.g1,this.o1,this.o1.edgeSet());
        for(DefaultEdge e : o1.edgeSet()){
            //get edge nodes
            SyntaxNode source = o1.getEdgeSource(e);
            SyntaxNode target = o1.getEdgeTarget(e);
            //build edge for g1
            this.g1.addEdge(source,target); 
        } 

        for(DefaultEdge e : o2.edgeSet()){
            //get edge nodes
            SyntaxNode source = o2.getEdgeSource(e);
            SyntaxNode target = o2.getEdgeTarget(e);
            //build edge for g1
            this.g2.addEdge(source,target); 
        } 


        this.initialise();
        this.isRenaming = this.exists(g1,g2); 
    }

    private void initialise(){
        //1. get iri nodes
        this.node2iri1 = this.getIRInodes(this.g1);
        this.node2iri2 = this.getIRInodes(this.g2); 

        //2. count occurrences of IRIs
        this.iri2occurrence1 = new HashMap<>();
        this.iri2occurrence2 = new HashMap<>();

        //3. recourd what iri corresponds to what nodes
        this.iri2nodes1 = new HashMap<>();
        this.iri2nodes2 = new HashMap<>();

        for(Map.Entry<SyntaxNode,IRI> entry : this.node2iri1.entrySet()){
            SyntaxNode n = entry.getKey();
            IRI iri = entry.getValue();

            this.iri2occurrence1.putIfAbsent(iri,0);
            int update = this.iri2occurrence1.get(iri) + 1;
            iri2occurrence1.replace(iri,update);

            this.iri2nodes1.putIfAbsent(iri,new HashSet<>());
            this.iri2nodes1.get(iri).add(n);
        }

        for(Map.Entry<SyntaxNode,IRI> entry : this.node2iri2.entrySet()){
            SyntaxNode n = entry.getKey();
            IRI iri = entry.getValue();
            this.iri2occurrence2.putIfAbsent(iri,0);

            int update = this.iri2occurrence2.get(iri) + 1;
            iri2occurrence2.replace(iri,update);

            this.iri2nodes2.putIfAbsent(iri,new HashSet<>());
            this.iri2nodes2.get(iri).add(n);
        }

        //3. add IRI counts to syntax nodes 
        for(SyntaxNode n : g1.vertexSet()){
            if(node2iri1.containsKey(n)){
                int occurrence = this.iri2occurrence1.get(node2iri1.get(n));
                n.setOccurrence(occurrence);
            }
        }

        for(SyntaxNode n : g2.vertexSet()){
            if(node2iri2.containsKey(n)){
                int occurrence = this.iri2occurrence2.get(node2iri2.get(n));
                n.setOccurrence(occurrence);
            }
        }

        //4. add renaming constraints
        for(IRI iri : this.iri2nodes1.keySet()){
            Set<SyntaxNode> nodes = this.iri2nodes1.get(iri);
            Set<SyntaxNode> visited = new HashSet<>();
            for(SyntaxNode n1 : nodes){
                for(SyntaxNode n2 : nodes){
                    if(n1 != n2 && !visited.contains(n2)){
                        RenamingEdge e = this.g1.addEdge(n1,n2); 
                        e.setRenamingConstraint();
                    }
                } 
                visited.add(n1);
            } 
        }

        for(IRI iri : this.iri2nodes2.keySet()){
            Set<SyntaxNode> nodes = this.iri2nodes2.get(iri);
            Set<SyntaxNode> visited = new HashSet<>();
            for(SyntaxNode n1 : nodes){
                for(SyntaxNode n2 : nodes){
                    if(n1 != n2 && !visited.contains(n2)){
                        RenamingEdge e = this.g2.addEdge(n1,n2); 
                        e.setRenamingConstraint();
                    }
                } 
                visited.add(n1);
            } 
        }
    }

    public boolean exists(){
        return this.isRenaming;
    }

    private boolean exists(Graph<SyntaxNode,RenamingEdge> g1, Graph<SyntaxNode,RenamingEdge> g2) { 
        //SyntaxNodeComparator nodeCom = new SyntaxNodeComparator();
        SyntaxNodeComparatorRenaming nodeCom = new SyntaxNodeComparatorRenaming();
        RenamingEdgeComparator edgeCom = new RenamingEdgeComparator();

        VF2GraphIsomorphismInspector inspector = new VF2GraphIsomorphismInspector(g1,g2,nodeCom,edgeCom,true);

        boolean isomorphic = inspector.isomorphismExists();
        if(!isomorphic)
            return false; 

        OccurrsCheck check = new OccurrsCheck(o1,o2);

        if(check.noRepeatedIRIs()){
            return isomorphic;
        }

        if(!check.satisfiesSizeConstraints()){
            return false;
        }

        //TODO: check whether types of IRI match

        //BOTH isomorphic && repetitive entities
        //so we need to test for renaming
        Iterator<GraphMapping<SyntaxNode,RenamingEdge>> it = inspector.getMappings();
        while(it.hasNext()){//as long as there are mappings
            GraphMapping<SyntaxNode,RenamingEdge> mapping = it.next();
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
        if(axims.equals(this.a2)){
            return true;
        }
        //for(OWLLogicalAxiom a : axims){
        //    if(a.equals(this.a2)){
        //        return true;
        //    }
        //}

        return false;
    }

    private Map<IRI,IRI> getValidRenaming(GraphMapping<SyntaxNode,RenamingEdge> m){
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

    private Map<SyntaxNode,IRI> getIRInodes(Graph<SyntaxNode,RenamingEdge> g){
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
