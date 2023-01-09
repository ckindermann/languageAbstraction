package uk.ac.man.cs.exp.snomed;


import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.util.*;
//import uk.ac.man.cs.pat.gg.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.subIso.*;

import uk.ac.man.cs.regularities.axiom.*;

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
public class Diseases {

    private static final Logger log = Logger.getLogger(String.valueOf(Diseases.class));

    public static void main(String[] args) throws IOException , Exception{

        String ontFilePath = args[0]; 
        //this file lists rare diseases?
        String diseaseFile = args[1]; //expecting this to be a list of diseases
        String outputPath = args[2]; 

        //quick hack to convert ontology from TTL to owlxml
        //log.info("\tChecking Ontology "); 
        //OntologyChecker checker = new OntologyChecker(ontFilePath, outputPath);
        //checker.isLoadable(new File(ontFilePath));
        //checker.isConvertible();
        //log.info("\tDone Checking"); 


        //Load ontology
        File ontFile = new File(ontFilePath);
        log.info("\tLoading Ontology : " + ontFile.getName()); 
        OntologyLoader ontLoader = new OntologyLoader(ontFile, true);
        OWLOntology ont = ontLoader.getOntology(); 

        //load file listing diseases
        Set<String> diseases = IOHelper.readFile(diseaseFile);

        MyTimer timer = new MyTimer(); 
        timer.go(); 
        GroundGeneralisationMiner miner = new GroundGeneralisationMiner(ont);
        //AxiomRegularityHierarchyImp hImp = new AxiomRegularityHierarchyImp(syntrees); 
        System.out.println(timer.stop("impHG"));
        
        Map<SyntaxTree,Set<OWLAxiom>> reg2ins = miner.getRegularity2instance();
        Map<OWLAxiom,SyntaxTree> axiom2reg = miner.getAxiom2regularity();

        //find disease classes
        String prefix = "http://snomed.info/id/";
        Set<OWLClass> classes = ont.getClassesInSignature(true);
        Set<OWLClass> diseasClasses = new HashSet<>(); 
        for(OWLClass c : classes){
            for(String d : diseases){
                //System.out.println(c.toString() + ":" + prefix+d);
                if(c.toString().equals("<"+prefix+d+">")){
                    diseasClasses.add(c);
                    break;
                } 
            } 
        }
        System.out.println("Finding classes done");

        //map class to regularity 
        //NB: this only works because SNOMED does not make use of classframes
        Map<OWLClass,OWLAxiom> disease2axiomDef = new HashMap<>(); 
        for (OWLAxiom a : axiom2reg.keySet()) {

            if(a instanceof OWLSubClassOfAxiom){
                OWLClassExpression cexp = ((OWLSubClassOfAxiom) a).getSubClass();
                if(cexp.isOWLClass()){
                    OWLClass cc = cexp.asOWLClass();
                    if(diseasClasses.contains(cc)){
                        disease2axiomDef.put(cc,a); 
                    }
                }
            }
            if(a instanceof OWLEquivalentClassesAxiom){
                Set<OWLClass> cls = ((OWLEquivalentClassesAxiom) a).getNamedClasses();
                for(OWLClass c : cls){
                    if(diseasClasses.contains(c)){
                        disease2axiomDef.put(c,a); 
                        break;
                    } 
                } 
            }
        }

        //check whether diseases form part of singleton regularities
        Set<SyntaxTree> rareDiseaseRegulariteis = new HashSet<>();
        for (Map.Entry<OWLClass, OWLAxiom> entry : disease2axiomDef.entrySet()) {
            OWLClass key = entry.getKey();
            OWLAxiom value = entry.getValue();

            rareDiseaseRegulariteis.add(axiom2reg.get(value));

            System.out.println(key.toString() + " " + reg2ins.get(axiom2reg.get(value)).size());
        }
        System.out.println("Number of Regularities" + rareDiseaseRegulariteis.size()); 
        for(SyntaxTree t : rareDiseaseRegulariteis){
            System.out.println(reg2ins.get(t).size());
        }


        //check how many regularities consist of "only" rare diseases 
        Set<SyntaxTree> onlyRareDiseaseRegulariteis = new HashSet<>();
        for(SyntaxTree t : rareDiseaseRegulariteis){
            Set<OWLAxiom> instances = reg2ins.get(t); 
            boolean only = true;
            for(OWLAxiom a : instances){ 
                if(a instanceof OWLSubClassOfAxiom){
                    OWLClassExpression cexp = ((OWLSubClassOfAxiom) a).getSubClass();
                    if(cexp.isOWLClass()){
                        OWLClass cc = cexp.asOWLClass();
                        if(!diseasClasses.contains(cc)){
                            only = false;
                            break;
                        }
                    }
                }
                if(a instanceof OWLEquivalentClassesAxiom){
                    Set<OWLClass> cls = ((OWLEquivalentClassesAxiom) a).getNamedClasses();
                    boolean found = false;
                    for(OWLClass c : cls){
                        if(diseasClasses.contains(c)){
                            found = true;
                            break;
                        } 
                    } 
                    if(!found){
                        only = false;
                    }
                }
            }
            if(only){
                onlyRareDiseaseRegulariteis.add(t);
            } 
        } 
        System.out.println("Number of pure rare disease regulariteis " + onlyRareDiseaseRegulariteis.size());
    }
}
