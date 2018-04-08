package org.springframework.samples.petclinic.owner;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import hashGenerator.HashGenerator;

public class OwnerRepositoryCSV implements OwnerRepository{
	//Owner format: id, firstname, lastname, address, city
	
	
	String ownerFile = "new-datastore/owners.csv";

	@Override
	public Collection<Owner> findByLastName(String lastName) {
		Collection<Owner> result = new ArrayList<Owner>();
		CSVReader ownerReader = null;
		try {
			ownerReader = new CSVReader(new FileReader(ownerFile));
			String[] row = ownerReader.readNext();
			while(row != null) {
				if(row.length > 0 && (row[2].equals(lastName) || lastName=="")) {
					//Found a matching last name
					Owner newOwner = constructOwner(row);
					result.add(newOwner);
				}
				row = ownerReader.readNext();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(ownerReader != null) {
				try {
					ownerReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	@Override
	public Owner findById(Integer id) {
		CSVReader ownerReader = null;
		try {
			ownerReader = new CSVReader(new FileReader(ownerFile));
			String [] row = ownerReader.readNext();
			while(row != null) {
				if(row.length > 0 && Integer.parseInt(row[0]) == id) {
					ownerReader.close();
					return constructOwner(row);
				}
				row = ownerReader.readNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(ownerReader != null) {
				try {
					ownerReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
	public Owner save(String firstName, String lastName, String address, String city, String telephone) {
		int ownerId = 0;
		try {
			ownerId = getCSVRow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	 
		Owner owner = new Owner();
		owner.setId(ownerId);
		owner.setFirstName(firstName);
		owner.setLastName(lastName);
		owner.setAddress(address);
		owner.setCity(city);
		owner.setTelephone(telephone);
		save(owner);
		return owner;
	}
	
    
    public void storeHashRecord() {//stores/update hashrecord
    	String filename ="hash-record/owners.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            HashGenerator hash = new HashGenerator();
    		String hashContent = hash.computeHash(OwnerController.appendHashedRows());//second hash
    			fw.append(hashContent);
				fw.append('\n');

            fw.flush();
            fw.close();
            System.out.println("CSV file for the owners table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	@Override
	public void save(Owner owner) {
    	String filename ="new-datastore/owners.csv";
        try {
            FileWriter fw = new FileWriter(filename, true);
            	
            //Append the new owner to the csv
            fw.append(Integer.toString(owner.getId()));
            fw.append(',');
            fw.append(owner.getFirstName());
            fw.append(',');
            fw.append(owner.getLastName());
            fw.append(',');
            fw.append(owner.getAddress());
            fw.append(',');
            fw.append(owner.getCity());
            fw.append(',');
            fw.append(owner.getTelephone());
            fw.append(',');
            fw.append('\n');
            fw.flush();
            fw.close();

            storeHashRecord();
            System.out.println("Shadow write for owner complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	
    public void updateOwner(Owner correctOwner, Owner ownerToBeUpdated) {
		CSVReader reader = null;
		int rowToUpdate = 0;
		try{
			reader = new CSVReader(new FileReader("new-datastore/owners.csv"));
	        List<String[]> csvBody = reader.readAll();
	        
	        for(String[] actual : csvBody) {
	        	//find the row of the owner to be updated, and' update with new owner info
	        	if (actual[0].equals(Integer.toString(ownerToBeUpdated.getId()))) {
	        		break;
	        	}
	        	rowToUpdate++;
	        }
	        
	        csvBody.get(rowToUpdate)[0] = correctOwner.getId().toString();
    		csvBody.get(rowToUpdate)[1] = correctOwner.getFirstName();
    		csvBody.get(rowToUpdate)[2] = correctOwner.getLastName();
    		csvBody.get(rowToUpdate)[3] = correctOwner.getAddress();
    		csvBody.get(rowToUpdate)[4] = correctOwner.getCity();
    		csvBody.get(rowToUpdate)[5] = correctOwner.getTelephone();
    		
	        reader.close();
	        // Write to CSV file
	        CSVWriter writer = new CSVWriter(new FileWriter("new-datastore/owners.csv"));
	        writer.writeAll(csvBody, false);
	        writer.flush();
	        writer.close();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private Owner constructOwner(String[] row) {
		Owner newOwner = new Owner();
		newOwner.setId(Integer.parseInt(row[0]));
		newOwner.setFirstName(row[1]);
		newOwner.setLastName(row[2]);
		newOwner.setAddress(row[3]);
		newOwner.setCity(row[4]);
		newOwner.setTelephone(row[5]);
		// also get the pets
		
		return newOwner;
	}
	
    public int getCSVRow() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/owners.csv"));
    	List<String[]> content = csvReader.readAll();
    	//Returning size + 1 to avoid id of 0
    	csvReader.close();
    	return content.size() + 1;
    }

}
