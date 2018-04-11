/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import com.opencsv.CSVReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.dbmigration.ConsistencyLogger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.concurrent.Future;
import java.util.Iterator;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
	private final PetRepository pets;
	private final OwnerRepository owners;
	private final PetRepositoryCSV csvPets = new PetRepositoryCSV();
	private final OwnerRepositoryCSV csvOwners = new OwnerRepositoryCSV();
	private final PetHashUpdater hashUpdater = new PetHashUpdater();

	@Autowired
	public PetController(PetRepository pets, OwnerRepository owners) {
		this.pets = pets;
		this.owners = owners;
		forklift();
		hashUpdater.storeHashRecord();
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.pets.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return this.owners.findById(ownerId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		} else {
			if(!ConsistencyLogger.shouldSwitchDatastore()) {
				this.pets.save(pet);
			}
			this.csvPets.save(pet);
			checkConsistency();
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(@PathVariable("petId") int petId, ModelMap model) {
		Pet pet = this.pets.findById(petId);
		Pet pet2 = csvPets.findById(petId);
		shadowReadConsistencyCheck(pet, pet2);
		if(ConsistencyLogger.shouldSwitchDatastore()) {
			pet = pet2;
		}
		model.put("pet", pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
		if (result.hasErrors()) {
			pet.setOwner(owner);
			model.put("pet", pet);
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		} else {
			owner.addPet(pet);
			if(!ConsistencyLogger.shouldSwitchDatastore()) {
				this.pets.save(pet);
			}
			this.csvPets.save(pet);
			checkConsistency();
			return "redirect:/owners/{ownerId}";
		}
	}

	@Async
	public void shadowReadConsistencyCheck(Collection<Pet> expected, Collection<Pet> actual) {
		Iterator<Pet> expectedPet = expected.iterator();
		for (Pet actualPet : actual){
			shadowReadConsistencyCheck(expectedPet.next(), actualPet);
		}
	}

	@Async
	public Future<Boolean> shadowReadConsistencyCheck(Pet expected, Pet actual) {
		boolean consistent = actual.isEqualTo(expected);
		ConsistencyLogger.logCheck();
		if (!consistent) {
			ConsistencyLogger.logInconsistent();
			System.out.println("Inconsistency found between Pets" + "\n" +
									expected.toString() + " and " + actual.toString());
			//TODO: update the row in the shadowread
			csvPets.updatePet(expected, actual);
		}
		return new AsyncResult<Boolean>(consistent);
	}

	//Implementation of method to move data from pets table to csv file
    public void forklift() {
    	String filename ="new-datastore/pets.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from pets";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                fw.append(rs.getString(1));
                fw.append(',');
                fw.append(rs.getString(2));
                fw.append(',');
                fw.append(rs.getString(3));
                fw.append(',');
                fw.append(rs.getString(4));
                fw.append(',');
                fw.append(rs.getString(5));
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV file for the pets table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Async
    @Scheduled(fixedDelay = 5000)
    public Future<Integer> checkConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/pets.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from pets";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            for(String[] actual : reader) {
                rs.next();
                for(int i=0;i<5;i++) {
                    int columnIndex = i+1;
                    if(!actual[i].equals(rs.getString(columnIndex))) {
                    	System.out.println("Pets Table Consistency Violation!\n" +
                				"\n\t expected = " + rs.getString(columnIndex)
                				+ "\n\t actual = " + actual[i]);
                    	//fix inconsistency
                    	actual[i] = rs.getString(columnIndex);
                        inconsistencies++;
                    }
                }
            }

            if (inconsistencies == 0)
            	System.out.println("No inconsistencies across former pets table dataset.");
            else
            	System.out.println("Number of Inconsistencies for Pets Table: " + inconsistencies);

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return new AsyncResult<Integer>(inconsistencies);
    }


    public void writeToMySqlDataBase(int petId, String name, java.sql.Date birthDate, int typeId, int ownerId) throws Exception {
    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        // the mysql insert statement
        String query = " INSERT into pets (id, name, birth_date, type_id, owner_id)"
          + " Values (?, ?, ?, ?, ?)";

        // Create the MySql insert query
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1, petId);
        preparedStmt.setString(2, name);
        preparedStmt.setDate(3, birthDate);
        preparedStmt.setInt(4, typeId);
        preparedStmt.setInt(5, ownerId);

        // execute the prepared statement
        preparedStmt.execute();
    }

	public void writeToFile(Pet pet) {
		pets.save(pet);
		csvPets.save(pet);
	}

    private int getCSVRow() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/pets.csv"));
    	List<String[]> content = csvReader.readAll();
    	csvReader.close();
    	//Returning size + 1 to avoid id of 0
    	return content.size() + 1;
    }

	public String readFromMySqlDataBase(int petId) {

    	StringBuilder stringBuilder = new StringBuilder();
    	try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
	        String query = "SELECT * FROM pets WHERE id=?";
	        PreparedStatement preparedSelect = conn.prepareStatement(query);
	        preparedSelect.setInt(1, petId);

	        ResultSet rs = preparedSelect.executeQuery();

	        while (rs.next()) {
	        	stringBuilder.append(Integer.toString(rs.getInt("id")) + ",");
	        	stringBuilder.append(rs.getString("name") + ",");
	        	stringBuilder.append(rs.getDate("birth_date") + ",");
	        	stringBuilder.append(rs.getInt("type_id") + ",");
	        	stringBuilder.append(rs.getInt("owner_id") + ",");
	        }
    	} catch (Exception e) {
            e.printStackTrace();
        }

        String petData = stringBuilder.toString();
    	return petData;
    }

    public String readFromNewDataStore(int petId) {
    	String petData = "";

    	try {
    		CSVReader reader = new CSVReader(new FileReader("new-datastore/pets.csv"));

    		for (String[] actual : reader) {
    			if (actual[0].equals(String.valueOf(petId))) {
    				for (int i = 0; i < 5; i++) {
    					petData += actual[i] + ",";
    				}
    			}
    		}

    	} catch (Exception e) {
            e.printStackTrace();
        }

    	return petData;
    }

}
