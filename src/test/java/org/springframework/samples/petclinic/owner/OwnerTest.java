package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.visit.Visit;

public class OwnerTest {

	private Owner ownerInstance;
	private Owner ownerForMocking;

	@Before
	public void setUp() {
		this.ownerInstance = new Owner();
		this.ownerForMocking = new Owner();
		this.ownerForMocking.setId(1);
		this.ownerForMocking.setFirstName("Mark");
		this.ownerForMocking.setLastName("Ghantous");
		this.ownerForMocking.setAddress("647 Abbey Road");
		this.ownerForMocking.setCity("Amoun");
		this.ownerForMocking.setTelephone("345123222");
	}
	
	
	@Test
	public void getSetTelephoneTest() {
		// Owner instance = new Owner();
		ownerInstance.setTelephone("514 371 9999");
		String result = ownerInstance.getTelephone();
		assertEquals("514 371 9999", result);
	}
	
	@Test
	public void setGetCityTest() {
		// Owner instance = new Owner();
		ownerInstance.setCity("Montreal");
		String result = ownerInstance.getCity();
		assertEquals("Montreal", result);
	}
	
	@Test
	public void toStringTest() {
		ToStringCreator creator = new ToStringCreator(ownerInstance);
		String expected = 
				creator
				.append("id", ownerInstance.getId())
				.append("new", ownerInstance.isNew())
				.append("lastName", ownerInstance.getLastName())
				.append("firstName", ownerInstance.getFirstName())
        			.append("address", ownerInstance.getAddress())
                .append("city", ownerInstance.getCity())
        			.append("telephone", ownerInstance.getTelephone())
				.toString();
		String result = ownerInstance.toString();
		assertEquals(expected, result); 
	}
	
	@Test
	public void setPetgetPetsTest() {
		Pet pet = new Pet();

		pet.setName("Pogo");
		ownerInstance.addPet(pet);
		List<Pet> result = ownerInstance.getPets();
		Pet onlyPet = result.iterator().next();

		assertEquals(1, result.size()); // Make sure there's only one element in the Collection returned	
		assertEquals(pet, onlyPet);
		assertEquals(pet.getName(), onlyPet.getName());
	}

	@Test
	public void getPetExistsTest() {	
		Pet pet = new Pet();
		pet.setName("Pochi");
		ownerInstance.addPet(pet);
		
		//tests pet object exists
		assertEquals(pet, ownerInstance.getPet("Pochi"));	
		assertEquals(pet, ownerInstance.getPet("Pochi", false));
	}
	
	@Test
	public void getPetDoesntExistsTest() {	
		Pet pet = new Pet();
		pet.setName("Pochi");
		ownerInstance.addPet(pet);
		//tests pet object doesn't exist
		assertEquals(null, ownerInstance.getPet("Pochi", true));	
	}
	
	@Test
	public void getPetsTest() {
		Pet pet = new Pet();
		pet.setName("Death");
		List<Pet> list = new ArrayList<>();
		list.add(pet);
		ownerInstance.addPet(pet);
		
		assertEquals(list.get(0).getName(), ownerInstance.getPets().iterator().next().getName()); 
		
		Pet pet2 = new Pet();
		pet2.setName("Dirty");
		list.add(pet2);
		ownerInstance.addPet(pet2);
		
		assertEquals(list.get(1).getName(), ownerInstance.getPets().get(1).getName());
	}
	
	@Test
	public void setGetAddress() {
		ownerInstance.setAddress("123 FakeStreet");
		assertEquals("123 FakeStreet", ownerInstance.getAddress());
	}
	
	//Mocking out pet dependency of owners for test pourposes
	@Test
    public void testMockPets() {
		//Mock all the different attributes of a Pet in order mock the Pet object as a whole
		//Date
        Date zebraBirthday = new Date(1992, 3, 9);

        //Type
		PetType zebra = mock(PetType.class);
        when(zebra.getName()).thenReturn("Zebra");
        
        //Type Id
        when(zebra.getId()).thenReturn(96);

        //A couple of visits for the pet in question
        Set<Visit> zebraVisits = new LinkedHashSet<>();
        Visit mockFirstZebraVisit = mock(Visit.class);
        Visit mockSecondZebraVisit = mock(Visit.class);
        zebraVisits.add(mockFirstZebraVisit);
        zebraVisits.add(mockSecondZebraVisit);

        //Creation of dog
        Pet mockZebra = mock(Pet.class);
        //Stub mock object constructor call
        when(mockZebra.isNew()).thenReturn(true);
        when(mockZebra.getBirthDate()).thenReturn(zebraBirthday);
        when(mockZebra.getType()).thenReturn(zebra);
        when(mockZebra.getName()).thenReturn("Jonathan Glazer");
        when(mockZebra.getOwner()).thenReturn(this.ownerForMocking);
        when(mockZebra.getVisitsInternal()).thenReturn(zebraVisits);
        
		//Mock all the different attributes of a Pet in order mock the Pet object as a whole
		//Date
        Date dogBirthday = new Date(1991, 7, 10);

        //Type
		PetType dog = mock(PetType.class);
        when(dog.getName()).thenReturn("Dog");
        
        //Type Id
        when(dog.getId()).thenReturn(102);

        //A couple of visits for the pet in question
        Set<Visit> dogVisits = new LinkedHashSet<>();
        Visit mockFirstDogVisit = mock(Visit.class);
        Visit mockSecondDogVisit = mock(Visit.class);
        dogVisits.add(mockFirstDogVisit);
        dogVisits.add(mockSecondDogVisit);

        //Creation of dog
        Pet mockDog = mock(Pet.class);
        //Stub mock object constructor call
        when(mockDog.isNew()).thenReturn(true);
        when(mockDog.getBirthDate()).thenReturn(dogBirthday);
        when(mockDog.getType()).thenReturn(dog);
        when(mockDog.getName()).thenReturn("Paul Thomas Anderson");
        when(mockDog.getOwner()).thenReturn(this.ownerForMocking);
        when(mockDog.getVisitsInternal()).thenReturn(dogVisits);
       
        //Add the mocks pets to the owner
        this.ownerForMocking.addPet(mockZebra);
        this.ownerForMocking.addPet(mockDog);

        //Ensure that the inserted pets are equal by comparing the name and Id of the pets
        assertThat(this.ownerForMocking.getPets().get(0)).isEqualTo(mockZebra);
        assertThat(this.ownerForMocking.getPets().get(0).getName()).isEqualTo("Jonathan Glazer");
        assertThat(this.ownerForMocking.getPets().get(0).getType().getId() == 96);
        assertThat(this.ownerForMocking.getPets().get(0).getType().getName()).isEqualTo("Zebra");
        assertThat(this.ownerForMocking.getPets().get(1)).isEqualTo(mockDog);
        assertThat(this.ownerForMocking.getPets().get(1).getName()).isEqualTo("Paul Thomas Anderson");
        assertThat(this.ownerForMocking.getPets().get(1).getType().getId() == 102);
        assertThat(this.ownerForMocking.getPets().get(1).getType().getName()).isEqualTo("Dog");
        
    }
}