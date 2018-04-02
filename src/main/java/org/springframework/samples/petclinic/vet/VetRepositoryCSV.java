package org.springframework.samples.petclinic.vet;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.opencsv.CSVReader;

public class VetRepositoryCSV implements VetRepository{

	@Override
	public Collection<Vet> findAll() throws DataAccessException {
		CSVReader vetReader = null;
		ArrayList<Vet> result = new ArrayList<>();
		String vetFile = "new-datastore/vets.csv";
		try {
			vetReader = new CSVReader(new FileReader(vetFile));
			List<String[]> vetData = vetReader.readAll();
			for(String[] row: vetData) {
				int vetId = Integer.parseInt(row[0]);
				List<Specialty> vetSpecs = getVetSpecs(vetId);
				Vet newVet = new Vet();
				newVet.setId(vetId);
				newVet.setFirstName(row[1]);
				newVet.setLastName(row[2]);
				
				for(Specialty spec: vetSpecs) {
					newVet.addSpecialty(spec);
				}
				
				result.add(newVet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				vetReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	private List<Specialty> getVetSpecs(int vetId) {
		CSVReader specReader = null;
		List<Specialty> specs = new ArrayList<>();
		String vetSpecFile = "new-datastore/vet-specialties.csv";
		try {
			specReader = new CSVReader(new FileReader(vetSpecFile));
			List<String[]> specData = specReader.readAll();
			for(String[] row : specData) {
				if(row.length == 2 && Integer.parseInt(row[1]) == vetId) {
					int specialtyId = Integer.parseInt(row[1]);
					Specialty newSpec = new Specialty();
					newSpec.setId(specialtyId);
					newSpec.setName(getSpecialtyName(specialtyId));
					specs.add(newSpec);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(specReader != null) {
			try {
				specReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return specs;
	}
	
	private String getSpecialtyName(int specId) {
		CSVReader specReader = null;
		String specFile = "new-datastore/specialties.csv";
		try {
			specReader = new CSVReader(new FileReader(specFile));
			String[] row = specReader.readNext();
			while(row != null) {
				if(row.length > 0 && Integer.parseInt(row[0]) == specId) 
				{
					specReader.close();
					return row[1];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(specReader != null) {
				try {
					specReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}

}
