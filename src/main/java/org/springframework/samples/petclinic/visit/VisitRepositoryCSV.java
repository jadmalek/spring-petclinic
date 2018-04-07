package org.springframework.samples.petclinic.visit;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.opencsv.CSVReader;

// Visit csv format: ID, PetId, Description, Date

public class VisitRepositoryCSV implements VisitRepository{
	String visitFile = "new-datastore/visits.csv";
	
	@Override
	public void save(Visit visit) throws DataAccessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Visit> findByPetId(Integer petId) {
		CSVReader visitReader = null;
		List<Visit> result = new ArrayList<>();
		
		try {
			visitReader = new CSVReader(new FileReader(visitFile));
			String[] row = visitReader.readNext();
			while(row != null) {
				if(row.length > 0 && Integer.parseInt(row[1]) == petId) {
					Visit newVisit = constructVisit(row);
					result.add(newVisit);
				}
				row = visitReader.readNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private Visit constructVisit(String[] row) {
		Visit visit = new Visit();
		visit.setId(Integer.parseInt(row[0]));
		visit.setPetId(Integer.parseInt(row[1]));
		visit.setDescription(row[2]);
		visit.setDate(new Date(Long.parseLong(row[3])));
		
		return visit;
	}

}
