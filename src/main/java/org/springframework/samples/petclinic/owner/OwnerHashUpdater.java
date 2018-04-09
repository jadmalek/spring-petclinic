package org.springframework.samples.petclinic.owner;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import com.opencsv.CSVReader;

import hashGenerator.HashGenerator;

public class OwnerHashUpdater {
	
    public String chainHashContent() {//collects rows, hashes them, appends them, hashes them again and returns
    	String hashContent = "";
    	HashGenerator hash = new HashGenerator();
    	
    	try {
    		CSVReader reader = new CSVReader(new FileReader("new-datastore/owners.csv"));

    		for(String[] actual : reader) {
    			ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(actual));
    			String row = arrayList.toString();
    			hashContent += hash.computeHash(row);
        		
         	} 
    		reader.close();
    	}catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
    	String secondHash = hash.computeHash(hashContent);
        return secondHash;
    }
    
    public void storeHashRecord() {//stores/update hashrecord
    	String filename ="hash-record/owners.csv";
        try {
            FileWriter fw = new FileWriter(filename);
    		String hashContent = chainHashContent();
    			fw.append(hashContent);
				fw.append('\n');

            fw.flush();
            fw.close();
            System.out.println("CSV file for the owners table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
}
