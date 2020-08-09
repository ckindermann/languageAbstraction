package uk.ac.man.cs.util;

//import uk.ac.man.cs.detectors.structural.*;
//

import java.io.*;
import java.util.*;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.HermiT.ReasonerFactory;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;

public class MyTimer {

    long startTime;
    long endTime; 

    public MyTimer(){
        this.startTime = System.nanoTime();
        this.endTime = System.nanoTime(); 
    }

    public void go(){
        this.startTime = System.nanoTime(); 
    }

    public void stop(String message){
        this.endTime = System.nanoTime(); 
       double duration = (this.endTime - this.startTime) / 1000000000.0;
       //if(duration > 1){
           System.out.println(message + " took " + duration + " seconds");
       //}
    } 

}
