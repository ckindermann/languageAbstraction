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


public class SetClassExpressionPreservance {

    public static boolean exists(Set<SyntaxTree> s1, Set<SyntaxTree> s2){

        if(s1.size() != s2.size()){
            return false;
        }

        Set<SyntaxTree> matched = new HashSet<>();

        for(SyntaxTree t : s1){//for all trees
            boolean found = false;
            for(SyntaxTree s : s2){//find an equivalent one
                if(!matched.contains(s)){//only match trees once
                    if(ClassExpressionPreservance.exists(t,s)){//isomorphism found
                        //check weights
                        found = true;
                        matched.add(s); 
                        break; 
                    }
                }
            }
            if(!found){
                return false;
            }
        }
        return true; //all trees could be matched
    } 

}
