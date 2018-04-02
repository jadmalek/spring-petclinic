package org.springframework.samples.petclinic.owner;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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

	@Override
	public void save(Owner owner) {
		// TODO Auto-generated method stub
		
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
	
}
