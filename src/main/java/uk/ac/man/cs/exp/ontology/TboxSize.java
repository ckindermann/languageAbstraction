package uk.ac.man.cs.exp.ontology;


import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;

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
public class TboxSize {

    private static final Logger log = Logger.getLogger(String.valueOf(TboxSize.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String outputPath = args[1];

        TboxSize exp = new TboxSize(); 
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

        int tboxSize = this.getTboxSize(ont);
        int classAxioms = this.getClassAxiomNumber(ont);
        IOHelper.writeAppend(ontologyName + "," + tboxSize + "," + classAxioms, output + "/tboxSize"); 
    } 

    private int getClassAxiomNumber(OWLOntology ont){

        int classAxioms = 0;
        classAxioms += ont.getAxioms(AxiomType.SUBCLASS_OF, Imports.INCLUDED).size();
        classAxioms += ont.getAxioms(AxiomType.EQUIVALENT_CLASSES, Imports.INCLUDED).size();
        classAxioms += ont.getAxioms(AxiomType.DISJOINT_UNION, Imports.INCLUDED).size();
        classAxioms += ont.getAxioms(AxiomType.DISJOINT_CLASSES, Imports.INCLUDED).size();
        return classAxioms;
    }

    private int getTboxSize(OWLOntology ont){ 
        Set<OWLAxiom> tBox = ont.getTBoxAxioms(Imports.INCLUDED); 
        Set<OWLAxiom> rBox = ont.getRBoxAxioms(Imports.INCLUDED);
        return tBox.size() + rBox.size();
    }
}
