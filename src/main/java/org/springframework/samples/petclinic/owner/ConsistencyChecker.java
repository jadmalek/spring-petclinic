package org.springframework.samples.petclinic.owner;

import java.io.FileReader;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;

@Component
public class ConsistencyChecker {
	
	@Scheduled(fixedDelay = 5000)
    public int checkOwnerConsistency() {//comparing hash of columns
    	int inconsistencies = 0;
    	OwnerHashUpdater hashUpdater = new OwnerHashUpdater();
		
		try {
			CSVReader hashReader = new CSVReader(new FileReader("hash-record/owners.csv"));
	    	String csvCurrent;
	    	String hashRecord = "";
            
	    	csvCurrent = hashUpdater.chainHashContent();
			
			for(String[] actual : hashReader) {
				hashRecord = actual[0];
			}
				
			if(!hashRecord.equals(csvCurrent)) {
				System.out.println("Consistency Violation!\n" + 
						"\n\t changes found in owners CSV");
		    	inconsistencies++;
		    }
			
			hashReader.close();

			if (inconsistencies == 0) 
            	System.out.println("No inconsistencies across former owners table dataset.");
			
    	}catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
		return inconsistencies;
    }
	
	@Scheduled(fixedDelay = 5000)
	public int checkPetsConsistency() {//comparing hash of columns
    	int inconsistencies = 0;
    	PetHashUpdater hashUpdater = new PetHashUpdater();
		
		try {
			CSVReader hashReader = new CSVReader(new FileReader("hash-record/pets.csv"));
	    	String csvCurrent;
	    	String hashRecord = "";
            
			csvCurrent = hashUpdater.chainHashContent();
			
			for(String[] actual : hashReader) {
				hashRecord = actual[0];
			}
				
			if(!hashRecord.equals(csvCurrent)) {
				System.out.println("Consistency Violation!\n" + 
						"\n\t changes found in pet-types CSV");
		    	inconsistencies++;
		    }
			
			hashReader.close();
			if (inconsistencies == 0) 
            	System.out.println("No inconsistencies across former pets table dataset.");
			
    	}catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
		return inconsistencies;
    }

}
