package org.springframework.samples.petclinic.owner;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Date;

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
        assertEquals(0,ownerController.checkConsistency());
        assertEquals(0,petController.checkConsistency());
        assertEquals(0,visitController.checkConsistency());
        assertEquals(0,vetController.checkConsistency());
        assertEquals(0,petType.checkConsistency());
        assertEquals(0,specialty.checkSpecialtiesConsistency());
        assertEquals(0,specialty.checkVetSpecialtiesConsistency());

		//shadow writes: any changes are written directly to old
		//consistency should be checked after each write
        ownerController.writeToFile("Bob", "Bobby", "808 Roberts", "Bobbytown", "545-555-5555");
        petController.writeToFile("Buddy", new Date(), 0, 0);
        petType.writeToFile("Doggo");
        visitController.writeToFile(1, new Date(), "An annual checkup");
        vetController.writeToFile("Sophia", "Squash");
        specialty.writeToFileVetSpecialties(1, 2);
        assertEquals(0, ownerController.checkConsistency());
        assertEquals(0, petController.checkConsistency());
        assertEquals(0, petType.checkConsistency());
        assertEquals(0, visitController.checkConsistency());
        assertEquals(0, vetController.checkConsistency());
        specialty.writeToFileSpecialties("Specialty1");
        assertEquals(0, specialty.checkSpecialtiesConsistency());
        assertEquals(0, specialty.checkVetSpecialtiesConsistency());


		//shadow Reads for validation (read will access both old and new)
		// old will provide response
		// consistency check that old == new

		//read and write from new datastore

		//long term consistency checker
	}

}
