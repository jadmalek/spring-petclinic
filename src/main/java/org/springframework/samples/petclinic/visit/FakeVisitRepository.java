package org.springframework.samples.petclinic.visit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.dao.DataAccessException;

public class FakeVisitRepository implements VisitRepository{
	private HashMap<Integer, Visit> visitMap;
	
	public FakeVisitRepository() {
		visitMap = new HashMap<Integer, Visit>();
	}
	
	public FakeVisitRepository(HashMap<Integer, Visit> visitMap) {
		this.visitMap = visitMap;
	}
	
	@Override
	public void save(Visit visit) {
		visitMap.put(visit.getId(), visit);
		
	}

	@Override
	public List<Visit> findByPetId(Integer petId) {
		List<Visit> visitsWithPetId = new ArrayList<Visit>();
		Visit visit;
		for (Integer id : visitMap.keySet()) {
			visit = visitMap.get(id);
			if (visit.getPetId() == petId){
				visitsWithPetId.add(visit);
			}
		}
		return visitsWithPetId;
	}
}

