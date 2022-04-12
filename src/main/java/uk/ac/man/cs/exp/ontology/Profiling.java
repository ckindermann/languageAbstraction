package uk.ac.man.cs.exp.ontology;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;

import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Profiling {

    private static final Logger log = Logger.getLogger(String.valueOf(Profiling.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        String outputPath = args[1];

        OntologyProfiler profiler = new OntologyProfiler(ontFilePath, false);
        String ontologyName = Paths.get(ontFilePath).getFileName().toString();

        boolean categorised = false;

        if(profiler.hasNoClassExpressionAxioms()){
            IOHelper.writeAppend(ontologyName, outputPath + "/noClassExpressionAxioms");
            categorised = true;
        }

        if(profiler.isHierarchy() && !categorised){
            IOHelper.writeAppend(ontologyName, outputPath + "/hierarchy");
            categorised = true;
        }

        if(profiler.isEL() && !categorised){
            IOHelper.writeAppend(ontologyName, outputPath + "/EL");
            categorised = true;
        }

        if(profiler.isNotEL() && !categorised){
            IOHelper.writeAppend(ontologyName, outputPath + "/rich"); 
            categorised = true;
        }

        if(!categorised){
            IOHelper.writeAppend(ontologyName, outputPath + "/error"); 
        } 
    }
}
