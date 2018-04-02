package org.springframework.samples.petclinic.owner;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.opencsv.CSVReader;

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
				if(row.length > 0 && row[2].equals(lastName)) {
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
            fw.append(owner.getAddress());
            fw.append(',');
            fw.append(owner.getTelephone());
            fw.append(',');
            fw.append('\n');
            fw.flush();
            fw.close();

            System.out.println("Shadow write for owner complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	
	public void updateOwner(Owner correctOwner, Owner ownerToBeUpdated) {
		CSVReader reader = null;
		try{
			reader = new CSVReader(new FileReader("new-datastore/owners.csv"));

	        for(String[] actual : reader) {
	        	//TODO: UPDATE THE OWNER ACCORDINGLY 
	        	//Find the row corresponding to "ownerToBeUpdated" and replace with "correctOwner"
	           /* for(int i=0;i<6;i++) {
	                int columnIndex = i+1;
	                if(actual[i].equals(ownerToBeUpdated.toString())) {
	                	System.out.println("Consistency Violation!\n" +
	            				"\n\t expected = " + rs.getString(columnIndex)
	            				+ "\n\t actual = " + actual[i]);
	                    //fix inconsistency
	                	//actual[i] =;
	                }
	            }*/
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	private Owner constructOwner(String[] row) {
		Owner newOwner = new Owner();
		newOwner.setId(Integer.parseInt(row[0]));
		newOwner.setFirstName(row[1]);
		newOwner.setLastName(row[2]);
		newOwner.setAddress(row[3]);
		newOwner.setCity(row[4]);
		// also get the pets
		
		return newOwner;
	}
	
    private int getCSVRow() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/owners.csv"));
    	List<String[]> content = csvReader.readAll();
    	//Returning size + 1 to avoid id of 0
    	csvReader.close();
    	return content.size() + 1;
    }

}
