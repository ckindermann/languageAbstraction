package uk.ac.man.cs.regularities.axiomsets.renaming;

import uk.ac.man.cs.ont.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.util.*;
import uk.ac.man.cs.iso.renaming.*;
import uk.ac.man.cs.iso.gg.*;
import uk.ac.man.cs.structure.*;
import uk.ac.man.cs.structure.nodes.*;
import uk.ac.man.cs.parser.*;
import uk.ac.man.cs.regularities.axiomsets.*;

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


public class ClassFrameRenamingStratum {

    //private Set<ClassFrameRenaming> frames;
    private ClassFrameRenaming frame;

    private Set<OWLAxiom> subclasses;
    private Set<OWLAxiom> equivalenceClasses;
    private Set<OWLAxiom> disjointUnions;
    private Set<OWLAxiom> disjointClasses;

    private int subclassAxiomsSpecificity;
    private int equivalenceAxiomsSpecificity;
    private int disjointUnionAxiomsSpecificity;
    private int disjointClassesAxiomsSpecificity;

    private int subclassAxioms;
    private int equivalenceAxioms;
    private int disjointUnionAxioms;
    private int disjointClassesAxioms;

    private int subclassAxiomsClasses;
    private int equivalenceAxiomsClasses;
    private int disjointUnionAxiomsClasses;
    private int disjointClassesAxiomsClasses;

    private int subclassAxiomsObjectProperties;
    private int equivalenceAxiomsObjectProperties;
    private int disjointUnionAxiomsObjectProperties;
    private int disjointClassesAxiomsObjectProperties;

    private int subclassAxiomsDatatypes;
    private int equivalenceAxiomsDatatypes;
    private int disjointUnionAxiomsDatatypes;
    private int disjointClassesAxiomsDatatypes;

    private int subclassAxiomsDataproperties;
    private int equivalenceAxiomsDataproperties;
    private int disjointUnionAxiomsDataproperties;
    private int disjointClassesAxiomsDataproperties;

    private int subclassAxiomsNestedClassExpressions;
    private int equivalenceAxiomsNestedClassExpressions;
    private int disjointUnionAxiomsNestedClassExpressions;
    private int disjointClassesAxiomsNestedClassExpressions;

    //TODO: extend for number of nested class exression types(!)

    public ClassFrameRenamingStratum(ClassFrameRenaming frame){
        this.frame = frame;
        //this.frames = new HashSet<>();
        //this.frames.add(frame);

        this.subclasses = initAxiomSet(frame.getSuperClasses());
        this.equivalenceClasses = initAxiomSet(frame.getEquivalences());
        this.disjointUnions = initAxiomSet(frame.getDisjointUnions());
        this.disjointClasses = initAxiomSet(frame.getDisjointClasses());

        //specificity
        this.subclassAxiomsSpecificity = frame.getSuperClassSpecificity();
        this.equivalenceAxiomsSpecificity = frame.getEquivalenceSpecificity();
        this.disjointUnionAxiomsSpecificity = frame.getDisjointUnionSpecificity();
        this.disjointClassesAxiomsSpecificity = frame.getDisjointClassesSpecificity();

        //number of axios
        this.subclassAxioms = this.subclasses.size();
        this.equivalenceAxioms = this.equivalenceClasses.size();
        this.disjointUnionAxioms = this.disjointUnions.size();
        this.disjointClassesAxioms = this.disjointClasses.size();

        this.subclassAxiomsClasses = getClassesInSignature(this.subclasses);
        this.equivalenceAxiomsClasses = getClassesInSignature(this.equivalenceClasses);
        this.disjointUnionAxiomsClasses = getClassesInSignature(this.disjointUnions);
        this.disjointClassesAxiomsClasses = getClassesInSignature(this.disjointClasses);

        this.subclassAxiomsObjectProperties = getObjectPropertiesInSignature(this.subclasses);
        this.equivalenceAxiomsObjectProperties = getObjectPropertiesInSignature(this.equivalenceClasses);
        this.disjointUnionAxiomsObjectProperties = getObjectPropertiesInSignature(this.disjointUnions);
        this.disjointClassesAxiomsObjectProperties = getObjectPropertiesInSignature(this.disjointClasses);

        this.subclassAxiomsDatatypes = getDatatypesInSignature(this.subclasses);
        this.equivalenceAxiomsDatatypes = getDatatypesInSignature(this.equivalenceClasses);
        this.disjointUnionAxiomsDatatypes = getDatatypesInSignature(this.disjointUnions);
        this.disjointClassesAxiomsDatatypes = getDatatypesInSignature(this.disjointClasses);

        this.subclassAxiomsDataproperties = getDataPropertiesInSignature(this.subclasses);
        this.equivalenceAxiomsDataproperties = getDataPropertiesInSignature(this.equivalenceClasses);
        this.disjointUnionAxiomsDataproperties = getDataPropertiesInSignature(this.disjointUnions);
        this.disjointClassesAxiomsDataproperties = getDataPropertiesInSignature(this.disjointClasses);

        this.subclassAxiomsNestedClassExpressions = getNestedClassExpressions(this.subclasses);
        this.equivalenceAxiomsNestedClassExpressions = getNestedClassExpressions(this.equivalenceClasses);
        this.disjointUnionAxiomsNestedClassExpressions = getNestedClassExpressions(this.disjointUnions);
        this.disjointClassesAxiomsNestedClassExpressions = getNestedClassExpressions(this.disjointClasses);
    }

    public ClassFrameRenaming getFrame(){
        return this.frame;
    }

    public boolean hasMember(ClassFrameRenaming frame){
        //specificity
        boolean member = true;

        if(this.subclassAxiomsSpecificity != frame.getSuperClassSpecificity() ||
           this.equivalenceAxiomsSpecificity != frame.getEquivalenceSpecificity() ||
           this.disjointUnionAxiomsSpecificity != frame.getDisjointUnionSpecificity() ||
           this.disjointClassesAxiomsSpecificity != frame.getDisjointClassesSpecificity())
            return false;

        Set<OWLAxiom> fsubclasses = initAxiomSet(frame.getSuperClasses());
        Set<OWLAxiom> fequivalenceClasses = initAxiomSet(frame.getEquivalences());
        Set<OWLAxiom> fdisjointUnions = initAxiomSet(frame.getDisjointUnions());
        Set<OWLAxiom> fdisjointClasses = initAxiomSet(frame.getDisjointClasses());

        //number of axios
        if(this.subclassAxioms != fsubclasses.size() ||
           this.equivalenceAxioms != fequivalenceClasses.size() ||
           this.disjointUnionAxioms != fdisjointUnions.size() ||
           this.disjointClassesAxioms != fdisjointClasses.size())
            return false;


        if(this.subclassAxiomsClasses != getClassesInSignature(fsubclasses)||
           this.equivalenceAxiomsClasses != getClassesInSignature(fequivalenceClasses)||
           this.disjointUnionAxiomsClasses != getClassesInSignature(fdisjointUnions)||
           this.disjointClassesAxiomsClasses != getClassesInSignature(fdisjointClasses))
            return false;

        if(this.subclassAxiomsObjectProperties != getObjectPropertiesInSignature(fsubclasses)||
           this.equivalenceAxiomsObjectProperties != getObjectPropertiesInSignature(fequivalenceClasses)||
           this.disjointUnionAxiomsObjectProperties != getObjectPropertiesInSignature(fdisjointUnions)||
           this.disjointClassesAxiomsObjectProperties != getObjectPropertiesInSignature(fdisjointClasses))
            return false;


        if(this.subclassAxiomsDatatypes != getDatatypesInSignature(fsubclasses)||
           this.equivalenceAxiomsDatatypes != getDatatypesInSignature(fequivalenceClasses)||
           this.disjointUnionAxiomsDatatypes != getDatatypesInSignature(fdisjointUnions)||
           this.disjointClassesAxiomsDatatypes != getDatatypesInSignature(fdisjointClasses))
            return false;

        if(this.subclassAxiomsDataproperties != getDataPropertiesInSignature(fsubclasses)||
           this.equivalenceAxiomsDataproperties != getDataPropertiesInSignature(fequivalenceClasses)||
           this.disjointUnionAxiomsDataproperties != getDataPropertiesInSignature(fdisjointUnions)||
           this.disjointClassesAxiomsDataproperties != getDataPropertiesInSignature(fdisjointClasses))
            return false;

        if(this.subclassAxiomsNestedClassExpressions != getNestedClassExpressions(fsubclasses)||
           this.equivalenceAxiomsNestedClassExpressions != getNestedClassExpressions(fequivalenceClasses)||
           this.disjointUnionAxiomsNestedClassExpressions != getNestedClassExpressions(fdisjointUnions)||
           this.disjointClassesAxiomsNestedClassExpressions != getNestedClassExpressions(fdisjointClasses))
            return false;

        //this.frames.add(frame);
        return true;

    }

    private int getNestedClassExpressions(Set<OWLAxiom> axioms){
        int res = 0;
        for(OWLAxiom a : axioms){
            res += a.getNestedClassExpressions().size();
        }
        return res; 
    }

    private int getClassesInSignature(Set<OWLAxiom> axioms){
        int res = 0;
        for(OWLAxiom a : axioms){
            res += a.getClassesInSignature().size();
        }
        return res;
    }

    private int getObjectPropertiesInSignature(Set<OWLAxiom> axioms){
        int res = 0;
        for(OWLAxiom a : axioms){
            res += a.getObjectPropertiesInSignature().size();
        }
        return res;
    }

    private int getDatatypesInSignature(Set<OWLAxiom> axioms){
        int res = 0;
        for(OWLAxiom a : axioms){
            res += a.getDatatypesInSignature().size();
        }
        return res;
    }

    private int getDataPropertiesInSignature(Set<OWLAxiom> axioms){
        int res = 0;
        for(OWLAxiom a : axioms){
            res += a.getDataPropertiesInSignature().size();
        }
        return res;
    }

    private Set<OWLAxiom> initAxiomSet(Set<SyntaxTree> trees){
        Set<OWLAxiom> axioms = new HashSet<>();
        for(SyntaxTree t : trees){
            axioms.add(((AxiomNode) t.getRoot()).getAxiom());
        } 
        return axioms;
    } 

    @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof ClassFrameRenamingStratum))
        return false;
      ClassFrameRenamingStratum ref = (ClassFrameRenamingStratum) obj;
      return this.hasMember(ref.getFrame());

   }

    @Override
    public int hashCode() { 
        //int hash = 23;
        int hash = 17;

        hash = hash * 31 + subclassAxiomsSpecificity;
        hash = hash * 31 + equivalenceAxiomsSpecificity;
        hash = hash * 31 + disjointUnionAxiomsSpecificity;
        hash = hash * 31 + disjointClassesAxiomsSpecificity;

        hash = hash * 31 + subclassAxioms;
        hash = hash * 31 + equivalenceAxioms;
        hash = hash * 31 + disjointUnionAxioms;
        hash = hash * 31 + disjointClassesAxioms;

        hash = hash * 31 + subclassAxiomsClasses;
        hash = hash * 31 + equivalenceAxiomsClasses;
        hash = hash * 31 + disjointUnionAxiomsClasses;
        hash = hash * 31 + disjointClassesAxiomsClasses;

        hash = hash * 31 + subclassAxiomsObjectProperties;
        hash = hash * 31 + equivalenceAxiomsObjectProperties;
        hash = hash * 31 + disjointUnionAxiomsObjectProperties;
        hash = hash * 31 + disjointClassesAxiomsObjectProperties;

        hash = hash * 31 + subclassAxiomsDatatypes;
        hash = hash * 31 + equivalenceAxiomsDatatypes;
        hash = hash * 31 + disjointUnionAxiomsDatatypes;
        hash = hash * 31 + disjointClassesAxiomsDatatypes;

        hash = hash * 31 + subclassAxiomsDataproperties;
        hash = hash * 31 + equivalenceAxiomsDataproperties;
        hash = hash * 31 + disjointUnionAxiomsDataproperties;
        hash = hash * 31 + disjointClassesAxiomsDataproperties;

        hash = hash * 31 + subclassAxiomsNestedClassExpressions;
        hash = hash * 31 + equivalenceAxiomsNestedClassExpressions;
        hash = hash * 31 + disjointUnionAxiomsNestedClassExpressions;
        hash = hash * 31 + disjointClassesAxiomsNestedClassExpressions;
        return hash;
    } 
}

