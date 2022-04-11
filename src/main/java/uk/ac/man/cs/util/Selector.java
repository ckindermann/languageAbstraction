package uk.ac.man.cs.util;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;
import java.util.*;

public final class Selector{

    public static void main(String[] args) throws Exception {
        File target = new File(args[0]);
        File from = new File(args[1]);
        String out = args[2]; 

        Set<String> res = selectTarget(target,from);
        IOHelper.writeStringSet(out, res); 
    }

    public static Set<String> selectTarget(File targets, File from) throws Exception {
        Set<String> keys = IOHelper.readFile(targets);
        Set<String> searchSpace = IOHelper.readFile(from);
        Set<String> selection = new HashSet<>();
        for(String s : searchSpace){ 
            String[] entry = s.split(",");
            if(keys.contains(entry[0])){
                selection.add(s);
            }
        } 
        return selection;
    }
}
