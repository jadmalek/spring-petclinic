package org.springframework.samples.petclinic.owner;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Date;

import org.springframework.samples.petclinic.vet.VetRepositoryCSV;
import org.springframework.samples.petclinic.owner.OwnerController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.vet.Specialty;
import org.springframework.samples.petclinic.vet.Vet;
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
    	specialty = new Specialty();
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
        petType.writeToFile("Doggo");
        petController.writeToFile("Buddy", new Date(), 1, 1);
        visitController.writeToFile(1, new Date(), "An annual checkup");
        vetController.writeToFile("Sophia", "Squash");
        specialty.writeToFileSpecialties("Specialty1");
        specialty.writeToFileVetSpecialties(1, 1);
        assertEquals(0, ownerController.checkConsistency());
        assertEquals(0, petController.checkConsistency());
        assertEquals(0, petType.checkConsistency());
        assertEquals(0, visitController.checkConsistency());
        assertEquals(0, vetController.checkConsistency());
        assertEquals(0, specialty.checkSpecialtiesConsistency());
        assertEquals(0, specialty.checkVetSpecialtiesConsistency());


		//shadow Reads for validation (read will access both old and new)

        assertEquals(ownerController.readFromMySqlDataBase(1), ownerController.readFromNewDataStore(1));
        assertEquals(petController.readFromMySqlDataBase(1), petController.readFromNewDataStore(1));
        assertEquals(petType.readFromMySqlDataBase(1), petType.readFromNewDataStore(1));
        assertEquals(visitController.readFromMySqlDataBase(1), visitController.readFromNewDataStore(1));
        assertEquals(vetController.readFromMySqlDataBase(1), vetController.readFromNewDataStore(1));
        assertEquals(specialty.readFromMySqlDataBaseSpecialties(1), specialty.readFromNewDataStore(1));
        

		// old will provide response
		// consistency check that old == new

		//read and write from new datastore

		//long term consistency checker
	}
    
    /*
     * This test is disabled because I'm unable to test it on my setup
     * however I leave it to show what a check might look like.
    @Test
    public void testVetRepoConsistency() {
    	vetController.forklift();
    	VetRepositoryCSV csvRepo = new VetRepositoryCSV();
    	Collection<Vet> sqlVets = vets.findAll();
    	Collection<Vet> csvVets = csvRepo.findAll();
    	assertEquals(sqlVets.size(), csvVets.size());
    	for(Vet item: sqlVets) {
    		assertTrue(csvVets.contains(item));
    	}
    }
    */

}
