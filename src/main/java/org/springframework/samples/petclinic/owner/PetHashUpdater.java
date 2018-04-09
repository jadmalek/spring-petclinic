package org.springframework.samples.petclinic.owner;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

import com.opencsv.CSVReader;

import hashGenerator.HashGenerator;

public class PetHashUpdater {
	
	public String appendHashedRows() {//collects rows, hashes them, appends them and returns
    	String hashContent = "";
    	
    	try {
    		CSVReader reader = new CSVReader(new FileReader("new-datastore/pets.csv"));
    		HashGenerator hash = new HashGenerator();

    		
    		for(String[] actual : reader) {
    			ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(actual));
    			String row = arrayList.toString();
    			hashContent += hash.computeHash(row);
        		
         	} 
    		reader.close();
    	}catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return hashContent;
    }
    
    public void storeHashRecord() {//stores/update hashvalue
    	String filename ="hash-record/pets.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            HashGenerator hash = new HashGenerator();
    		String hashContent = hash.computeHash(appendHashedRows());//second hash
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
