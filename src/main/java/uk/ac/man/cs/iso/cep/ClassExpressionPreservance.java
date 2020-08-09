package uk.ac.man.cs.iso.cep;

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


public class ClassExpressionPreservance {

    public static boolean exists(SyntaxTree t1, SyntaxTree t2){
        Graph<SyntaxNode,DefaultEdge> g1 = t1.getTree();
        Graph<SyntaxNode,DefaultEdge> g2 = t2.getTree();

        return exists(g1,g2); 
    }

    public static boolean exists(Graph<SyntaxNode,DefaultEdge> g1, Graph<SyntaxNode,DefaultEdge> g2){
        SyntaxNodeComparator nodeCom = new SyntaxNodeComparator();
        DefaultEdgeComparator edgeCom = new DefaultEdgeComparator();
        VF2GraphIsomorphismInspector inspector = new VF2GraphIsomorphismInspector(g1,g2,nodeCom,edgeCom,true);

        return inspector.isomorphismExists(); 
    } 
}
