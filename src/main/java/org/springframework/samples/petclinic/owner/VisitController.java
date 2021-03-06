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
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

    private final VisitRepository visits;
    private final PetRepository pets;


    @Autowired
    public VisitController(VisitRepository visits, PetRepository pets) {
        this.visits = visits;
        this.pets = pets;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /**
     * Called before each and every @RequestMapping annotated method.
     * 2 goals:
     * - Make sure we always have fresh data
     * - Since we do not use the session scope, make sure that Pet object always has an id
     * (Even though id is not part of the form fields)
     *
     * @param petId
     * @return Pet
     */
    @ModelAttribute("visit")
    public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
        Pet pet = this.pets.findById(petId);
        model.put("pet", pet);
        Visit visit = new Visit();
        pet.addVisit(visit);
        return visit;
    }

    // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
    @GetMapping("/owners/*/pets/{petId}/visits/new")
    public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
        return "pets/createOrUpdateVisitForm";
    }

    // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
    @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
    public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
        if (result.hasErrors()) {
            return "pets/createOrUpdateVisitForm";
        } else {
            this.visits.save(visit);
            return "redirect:/owners/{ownerId}";
        }
    }

    //Implementation of method to move data from the visits table to a csv file
    public void forklift() {
    	String filename ="new-datastore/visits.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from visits";
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
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV file for the visits table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    @Scheduled(fixedDelay = 5000)
    public Future<Integer> checkConsistency() {
        int inconsistencies = 0;
        
        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/visits.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from visits";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            for(String[] actual : reader) {
                rs.next();
                for(int i=0;i<4;i++) {
                    int columnIndex = i+1;
                    ConsistencyLogger.logCheck();
                    if(!actual[i].equals(rs.getString(columnIndex))) {
                    	System.out.println("Visits Table Consistency Violation!\n" + 
                				"\n\t expected = " + rs.getString(columnIndex)
                				+ "\n\t actual = " + actual[i]);
                    	//fix inconsistency
                    	actual[i] = rs.getString(columnIndex);
                        inconsistencies++;
                        ConsistencyLogger.logInconsistent();
                    }
                }
            }
            
            if (inconsistencies == 0) 
            	System.out.println("No inconsistencies across former visits table dataset.");
            else
            	System.out.println("Number of Inconsistencies for Visits Table: " + inconsistencies);
            
        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return new AsyncResult<Integer>(inconsistencies);
    }
    
    public void writeToMySqlDataBase(int visitId, int petId, java.sql.Date visitDate, String description) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        String query = " INSERT into visits (id, pet_id, visit_date, description)"
          + " Values (?, ?, ?, ?)";

        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1,  visitId);
        preparedStmt.setInt(2, petId);
        preparedStmt.setDate(3, visitDate);
        preparedStmt.setString(4, description);

        preparedStmt.execute();
    }

    public void writeToFile(Visit visit) {
    	visits.save(visit);
    }
    
    private int getCSVRow() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/visits.csv"));
    	List<String[]> content = csvReader.readAll();
    	//Returning size + 1 to avoid id of 0
    	csvReader.close();
    	return content.size() + 1;
    }
    
    public String readFromMySqlDataBase(int visitId) {

    	StringBuilder stringBuilder = new StringBuilder();
    	try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
	        String query = "SELECT * FROM visits WHERE id=?";
	        PreparedStatement preparedSelect = conn.prepareStatement(query);
	        preparedSelect.setInt(1, visitId);

	        ResultSet rs = preparedSelect.executeQuery();

	        while (rs.next()) {
	        	stringBuilder.append(Integer.toString(rs.getInt("id")) + ",");
	        	stringBuilder.append(Integer.toString(rs.getInt("pet_id")) + ",");
	        	stringBuilder.append(rs.getDate("visit_date") + ",");
	        	stringBuilder.append(rs.getString("description") + ",");
	        }
    	} catch (Exception e) {
            e.printStackTrace();
        }
    	
    	System.out.println("visit readfrommysql " + stringBuilder.toString());
        String visitData = stringBuilder.toString();
    	return visitData;
    }

    public String readFromNewDataStore(int visitId) {
    	String visitData = "";

    	try {
    		CSVReader reader = new CSVReader(new FileReader("new-datastore/visits.csv"));

    		for (String[] actual : reader) {
    			if (actual[0].equals(String.valueOf(visitId))) {
    				for (int i = 0; i < 4; i++) {
    					visitData += actual[i] + ",";
    				}
    			}
    		}
    		reader.close();

    	} catch (Exception e) {
            e.printStackTrace();
        }
    	return visitData;
    }

}