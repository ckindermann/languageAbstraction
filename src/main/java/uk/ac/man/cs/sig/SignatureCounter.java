package uk.ac.man.cs.sig; 

import org.semanticweb.owlapi.model.*;
import java.nio.file.*;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.hierarchy.axioms.gg.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.subIso.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader; 
import com.opencsv.exceptions.CsvException;
import java.util.Arrays;

import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.*;

import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.*;

import java.util.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public final class SignatureCounter {

    private static final Logger log = Logger.getLogger(String.valueOf(SignatureCounter.class));

    public static void main(String[] args) throws Exception {
        String ontFilePath = args[0]; //ontotlogy containing regularity instances
        String outputPath = args[1];

        //get ontFileName
        String ontologyName = Paths.get(ontFilePath).getFileName().toString();
        //create folder
        outputPath = outputPath + "/" + ontologyName;
        IOHelper.createFolder(outputPath); 

        //Load ontology
        File ontFile = new File(ontFilePath);
        log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        TreeBuilder treeBuilder = new SyntaxTreeBuilder();

        Set<OWLAxiom> toTest = new HashSet<>();
        toTest.addAll(ont.getAxioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED));
        toTest.addAll(ont.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED));
        toTest.addAll(ont.getAxioms(AxiomType.DISJOINT_UNION, Imports.INCLUDED));
        toTest.addAll(ont.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.INCLUDED));

        //Build syntax trees
        Set<SyntaxTree> syntrees = new HashSet<>();
        for(OWLAxiom a : toTest){
            syntrees.add(treeBuilder.build(a));
        } 

        HashMap<OWLObject,Integer> entity2occurrence = new HashMap<>();

        for(SyntaxTree tree : syntrees){
            Set<SyntaxNode> nodes  = tree.getTree().vertexSet();
            for(SyntaxNode node : nodes){
                OWLObject object = node.getObject();
                if (object != null && object.isNamed()){
                    entity2occurrence.putIfAbsent(object, 0);
                    entity2occurrence.put(object, entity2occurrence.get(object) + 1);
                }
            }
        }

        for (Map.Entry<OWLObject, Integer> set : entity2occurrence.entrySet()) { 
            System.out.println(set.getKey() + "," + set.getValue());
        } 
    }
}
