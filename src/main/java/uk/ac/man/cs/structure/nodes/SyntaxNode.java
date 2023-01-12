package uk.ac.man.cs.structure.nodes;

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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;

/**
 * Syntax Node
 * A syntax node 
 */
public class SyntaxNode {
    //will hold
    //-class expressions
    //-property expressions
    //-numbers
    //-individuals
    //-axioms

    //TODO: add convenience functions to determine
    //whether a SyntaxNode contains an entity with an IRI
    private OWLObject object;

    //count number of times this node occurrs in an axiom
    //NB: I currently only use this for IRI's to speed up renaming detection
    private int occurrence;

    public SyntaxNode(OWLObject o){
        this.object = o;
        this.occurrence = 1;
    }

    public int getOccurrence(){
        return this.occurrence;
    }

    public void setOccurrence(int occ){
        this.occurrence = occ;
    }

    public String toString(){
        if(object == null){
            if(this instanceof CardinalityNode) {
                CardinalityNode c = (CardinalityNode) this;
                return c.toString();
            }
            return "null";
        } else {
            return this.object.toString();
        }
    }

    public OWLObject getObject(){
        return this.object;
        //what happens if I call "getClass()" on object?
        //TEST LATER
    } 
}
