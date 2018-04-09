package org.springframework.samples.petclinic.owner;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import com.opencsv.CSVReader;

import hashGenerator.HashGenerator;

public class PetHashUpdater {
	
	public String chainHashContent() {//collects rows, hashes them, appends them, hashes them again and returns
    	String hashContent = "";
    	HashGenerator hash = new HashGenerator();
    	
    	try {
    		CSVReader reader = new CSVReader(new FileReader("new-datastore/pets.csv"));
    		
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
    
    public void storeHashRecord() {//stores/update hashvalue
    	String filename ="hash-record/pets.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            String hashContent = chainHashContent();
    		
    			fw.append(hashContent);
				fw.append('\n');

            fw.flush();
            fw.close();
            System.out.println("CSV file for the pets table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
