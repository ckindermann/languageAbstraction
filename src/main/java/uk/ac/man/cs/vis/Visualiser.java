package uk.ac.man.cs.vis;


import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;

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

import java.nio.file.*;

import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

/**
 * A class to demonstrate the functionality of the library.
 */
public class Visualiser {

    private static final Logger log = Logger.getLogger(String.valueOf(Visualiser.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String outputPath = args[1];

        Visualiser exp = new Visualiser(); 
        exp.run(ontFilePath, outputPath); 
    }

    private void run(String ontFilePath, String output) throws Exception {
        LogManager.getLogManager().reset();//stupid module extractor is chatty

        File ontFile = new File(ontFilePath);
        log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        String path = output + "/" + ontologyName;

        SyntaxTreeBuilder fullBuilder = new SyntaxTreeBuilder();//original builder
        ConstructorPreservanceBuilder constructorBuilder = new ConstructorPreservanceBuilder();
        CEPreservanceBuilder cepBuilder = new CEPreservanceBuilder();

        int examples = 5; 
        int count = 1;
        for(OWLAxiom a : ont.getTBoxAxioms(Imports.INCLUDED)){
            if(count++ > examples){
                break;
            } 

            if(! ((a instanceof OWLSubClassOfAxiom) ||
                  (a instanceof OWLEquivalentClassesAxiom) ||
                  (a instanceof OWLDisjointClassesAxiom) ||
                  (a instanceof OWLDisjointUnionAxiom))){
                continue;
              }

            SyntaxTree t1 = fullBuilder.build(a);
            SyntaxTree t2 = constructorBuilder.build(a);
            SyntaxTree t3 = cepBuilder.build(a);

            System.out.println("----Full----"); 
            this.printTree(t1);
            System.out.println("----Full----"); 
            System.out.println("----constructor----"); 
            this.printTree(t2);
            System.out.println("----constructor----"); 
            System.out.println("----CEP----"); 
            this.printTree(t3);
            System.out.println("----CEP----"); 
        }
    }

    public static void printTree(SyntaxTree t){ 
        SyntaxNode root = t.getRoot();
        SimpleDirectedGraph<SyntaxNode,DefaultEdge> graph = t.getTree();

        //start with root iterate through 
        Set<SyntaxNode> currentLevel = new HashSet<>();
        Set<SyntaxNode> nextLevel = new HashSet<>();
        currentLevel.add(root);
        int depth = 0;
        while(!currentLevel.isEmpty()){
            for(SyntaxNode n : currentLevel){ 
                Set<DefaultEdge> toChildren = graph.outgoingEdgesOf(n);
                for(DefaultEdge e : toChildren){
                    SyntaxNode target = graph.getEdgeTarget(e);
                    String format = repeat("\t",depth) +
                                    n.toString() +
                                    "->" +
                                    target.toString();
                    System.out.println(format); 
                    nextLevel.add(target);
                } 
            }
            currentLevel.clear();
            currentLevel.addAll(nextLevel);
            nextLevel.clear();
            depth++;
        } 
    }

    public static String repeat(String s, int n){
        String res = "";
        for(int i = 0; i<n; i++){
            res+=s;
        }
        return res;
    }

}
