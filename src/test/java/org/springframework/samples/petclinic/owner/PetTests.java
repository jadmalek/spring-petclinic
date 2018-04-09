package org.springframework.samples.petclinic.owner;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.samples.petclinic.visit.Visit;

import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.time.*;
import java.util.Date;

public class PetTests {

	private Pet pet;
	private Date birthDate;

	@Before
	public void testSetUp() {
		//Initialization of pet
		this.pet = new Pet();
		PetType dog = new PetType();
		dog.setId(9);
		dog.setName("Duncan Jones");
		//Initialization of birthDate
		//Converting the current time to a local date and ultimately a date to be input into setBirthDate;
		LocalDateTime timePoint = LocalDateTime.now();
		LocalDate localDate = timePoint.toLocalDate();
		birthDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	@Test
	public void testSetAndGetBirthDate() {
		pet.setBirthDate(this.birthDate);
		Date resultOfGetDate = pet.getBirthDate();
		assertEquals(resultOfGetDate, this.birthDate);
	}

	@Test
	public void testSetAndGetType() {
		//Creating a new pet type to test the setters and getters for pet's type
		PetType walrus = new PetType();
		walrus.setId(36);
		walrus.setName("Alex Garland");
		pet.setType(walrus);
		PetType resultOfGetType = pet.getType();
		assertEquals(resultOfGetType.getName(), walrus.getName());
	}

	@Test
	public void testSetAndGetOwner() {
		//Creating a new owner type to test the setters and getters for the pet's owner
		Owner amandeepBhandal = new Owner();
		amandeepBhandal.setAddress("Off-world Colony");
		amandeepBhandal.setCity("Beirut");
		amandeepBhandal.setTelephone("514-333-3333");
		//Attach the newly created owner to the pet
		pet.setOwner(amandeepBhandal);
		Owner resultOfGetOwner = pet.getOwner();
		assertEquals("Off-world Colony", resultOfGetOwner.getAddress());
		assertEquals("Beirut", resultOfGetOwner.getCity());
		assertEquals("514-333-3333", resultOfGetOwner.getTelephone());
		assertEquals(0, resultOfGetOwner.getPetsInternal().size());
	}

	@Test
	public void testSetAndGetVisitsInternal() {
		//Creating a new set of visits, albeit an empty set, to test the setters and getters for the pet's visits
		Set<Visit> visitsForTesting = new LinkedHashSet<>();
		pet.setVisitsInternal(visitsForTesting);
		Set<Visit> resultOfGetVisitsInternal = pet.getVisitsInternal();
		assertEquals(resultOfGetVisitsInternal.size(), visitsForTesting.size());
	}

	@Test
	public void testAddVisitAndGetVisits() {
		//Creating a new set of visits, albeit an empty set, to test the setters and getters for the pet's visits
		Visit visitForTesting = new Visit();
		pet.addVisit(visitForTesting);
		List<Visit> resultOfGetVisits = pet.getVisits();
		Visit onlyVisitInCollection = resultOfGetVisits.iterator().next();
		assertEquals(1, resultOfGetVisits.size());
		assertEquals(onlyVisitInCollection.getId(), visitForTesting.getId());
	}
}