package org.springframework.samples.petclinic.owner;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.owner.VisitController;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.samples.petclinic.visit.FakeVisitRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import static org.mockito.Mockito.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 */
@RunWith(SpringRunner.class)
@WebMvcTest(VisitController.class)
public class VisitControllerTests {

    private static final int TEST_PET_ID = 1;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VisitRepository visits;

    @MockBean
    private PetRepository pets;

    @Before
    public void init() {
        given(this.pets.findById(TEST_PET_ID)).willReturn(new Pet());
    }

    @Test
    public void testInitNewVisitForm() throws Exception {
        mockMvc.perform(get("/owners/*/pets/{petId}/visits/new", TEST_PET_ID))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
        verify(pets).findById(anyInt()); //Ensure that a pet gets loaded per visit
    }

    @Test
    public void testProcessNewVisitFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
            .param("name", "George")
            .param("description", "Visit Description")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
        verify(pets).findById(anyInt()); //Ensure that a pet gets loaded per visit
        verify(visits).save(any(Visit.class)); //Ensure that upon success, the visit gets saved
    }

    @Test
    public void testProcessNewVisitFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/*/pets/{petId}/visits/new", TEST_PET_ID)
            .param("name", "George")
        )
            .andExpect(model().attributeHasErrors("visit"))
            .andExpect(status().isOk())
            .andExpect(view().name("pets/createOrUpdateVisitForm"));
        verify(pets).findById(anyInt()); //Ensure that a pet gets loaded per visit
    }
    
    @Test
    public void testMockVisitRepository() {
    	//Use the fake repository to break dependency of visit controller on visit repository
    	FakeVisitRepository fakeVisits = new FakeVisitRepository();
    	
    	//Creation of visit controller with fake visit repository and mocked pets by mockMVC
    	VisitController visitController = new VisitController(fakeVisits, pets);
    	
    	//Creation of visit to add to visits repo and mocked result to invoke a visit controller method that involves
    	//the use of the visits repo
    	Visit visit = mock(Visit.class);
    	when(visit.getId()).thenReturn(22);
    	when(visit.getPetId()).thenReturn(1);
    	BindingResult result = mock(BindingResult.class);
    	when(result.hasErrors()).thenReturn(false);
    	
    	//internally this will use the fake visit repository's save method; other methods rely on the mocked pets repository
    	visitController.processNewVisitForm(visit, result);
    	
    	//Check that save method was executed by checking that the id of the visit has now been stored
    	assertEquals(visit.getId().intValue(), fakeVisits.findByPetId(1).get(0).getId().intValue());
    }
   
}
