package org.springframework.samples.petclinic.owner;

import org.junit.Before;
import org.junit.Test;

import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.VetController;
import org.springframework.samples.petclinic.visit.VisitRepository;

public class TestMigrationConsistencyChecking {
    private OwnerRepository owners;
    private PetRepository pets;
    private VisitRepository visits;
    private VetRepository vets;
    private PetType petType;
    private Specialty specialty;
    
    private OwnerController ownerController;
    private PetController petController;
    private VisitController visitController;
    private VetController vetController;
    
    @Before
    public void setup() {
    	ownerController = new OwnerController(owners);
    	petController = new PetController(pets, owners);
    	visitController = new VisitController(visits, pets);
    	vetController = new VetController(vets);
    	petType = new PetType();
    	specialty = new Specialty();;
    }
    
    @Test
	public void test() {
		//Forklifting of data from owners, pets, vets, visits, types, specialties and vet-specialties tables into csv files
		ownerController.forklift();
		petController.forklift();
		visitController.forklift();
		vetController.forklift();
		petType.forklift();
		specialty.forkliftSpecialties();
		specialty.forkliftVetSpecialties();
		
		//check consistency of new with old (incremental replication)
		
		//shadow writes: any changes are written directly to old
		//consistency should be checked after each write
		
		//shadow Reads for validation (read will access both old and new)
		// old will provide response
		// consistency check that old == new
		
		//read and write from new datastore
		
		//long termn consistency checker
	}

}
