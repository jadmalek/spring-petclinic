package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class FakeOwnerRepository implements OwnerRepository{
	private HashMap<Integer, Owner> ownerMap;
	
	public FakeOwnerRepository() {
		ownerMap = new HashMap<Integer, Owner>();
	}
	
	public FakeOwnerRepository(HashMap<Integer, Owner> ownerMap) {
		this.ownerMap = ownerMap;
	}
	

	@Override
	public Collection<Owner> findByLastName(String lastName) {
		List<Owner> ownerWithLastName = new ArrayList<Owner>();
		Owner owner;
		for (Integer id : ownerMap.keySet()) {
			owner = ownerMap.get(id);
			if (owner.getLastName().equals(lastName)){
				ownerWithLastName.add(owner);
			}
		}
		return ownerWithLastName;
	}

	@Override
	public Owner findById(Integer id) {
		return ownerMap.get(id);
	}

	@Override
	public void save(Owner owner) {
		ownerMap.put(owner.getId(), owner);	
	}
	
	public int getOwnerId(Owner owner) {
		return owner.getId();
	}

}
