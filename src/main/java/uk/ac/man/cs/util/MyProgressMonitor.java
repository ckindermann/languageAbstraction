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

public class MyProgressMonitor {

    double work;
    double progress;
    double oldProgress;
    double step;

    String message;

    long startTime;
    long endTime; 

    public MyProgressMonitor(String m, double w, double s){
        this.work = w;
        this.step = s;
        this.progress = 0;
        this.oldProgress = 0;
        this.message = m;
        this.startTime = System.nanoTime();
        this.endTime = System.nanoTime(); 
    }

    public void update(double u){
       this.endTime = System.nanoTime(); 
       this.progress += u;

       if(progress/work >= oldProgress + step){
           double duration = (this.endTime - this.startTime) / 1000000000.0; 
           System.out.println(this.message + " : " + progress/work + " took " + duration + " seconds");
           oldProgress = progress/work;
       }
    } 

}
