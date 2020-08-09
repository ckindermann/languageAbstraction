package uk.ac.man.cs.ont;

/**
 * Created by slava on 15/09/17.
 */
public enum ReasonerName {
    // Valid reasoners
    HERMIT("HERMIT"),
    FACT("FACT"),
    PELLET("PELLET"),
    JFACT("JFACT"),
    TROWL("TROWL"),
    SNOROCKET("SNOROCKET"), //EL++
    ELK("ELK");//EL

    private final String name;

    ReasonerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ReasonerName get(String name){
        if(name.equals("HERMIT"))
            return ReasonerName.HERMIT; 
        if(name.equals("PELLET"))
            return ReasonerName.PELLET;
        if(name.equals("JFACT"))
            return ReasonerName.JFACT;

        //default is HERMIT
        return ReasonerName.HERMIT;
    }

}
