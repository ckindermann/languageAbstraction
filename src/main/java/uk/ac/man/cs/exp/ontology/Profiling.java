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
        System.out.println("NoLogicalAxioms " + profiler.isLogicallyEmpty());
        System.out.println("Hierarchy " + profiler.isHierarchy());
        System.out.println("EL " + profiler.isEL());
        System.out.println("Not EL " + profiler.isNotEL());
    }
}
