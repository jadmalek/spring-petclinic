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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

import java.io.FileReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
@EnableAsync
class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final OwnerRepository owners;
    private final OwnerRepositoryCSV csvOwners = new OwnerRepositoryCSV();

    @Autowired
    public OwnerController(OwnerRepository clinicService) {
        this.owners = clinicService;
        forklift();
    }
    
    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }


    @GetMapping("/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            this.owners.save(owner);
            this.csvOwners.save(owner);

            checkConsistency();
            return "redirect:/owners/" + owner.getId();
        }
    }

    @GetMapping("/owners/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        return "owners/findOwners";
    }

    @GetMapping("/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model) {

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.owners.findByLastName(owner.getLastName());
        Collection<Owner> csvResults = this.csvOwners.findByLastName(owner.getLastName());
        shadowReadConsistencyCheck(results, csvResults);
        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        } else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        } else {
            // multiple owners found
            model.put("selections", results);
            return "owners/ownersList";
        }
    }

    @GetMapping("/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.owners.findById(ownerId);
        Owner owner2 = csvOwners.findById(ownerId);
        shadowReadConsistencyCheck(owner, owner2);
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping("/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            owner.setId(ownerId);
            this.owners.save(owner);
            this.csvOwners.save(owner);
            try {
            	this.writeToMySqlDataBase(owner);
            } catch (Exception e) {
            	e.printStackTrace();
            }

            checkConsistency();
            return "redirect:/owners/{ownerId}";
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        Owner expectedOwner = this.owners.findById(ownerId);
        mav.addObject(expectedOwner);
        Owner actualOwner = this.csvOwners.findById(ownerId);
        shadowReadConsistencyCheck(expectedOwner, actualOwner);
        return mav;
    }

    @Async
    public void shadowReadConsistencyCheck(Collection<Owner> expected, Collection<Owner> actual) {
    	Iterator<Owner> expectedOwner = expected.iterator();
    	for (Owner actualOwner : actual){
    		shadowReadConsistencyCheck(expectedOwner.next(), actualOwner);
    	}
    }

    @Async
    public Future<Boolean> shadowReadConsistencyCheck(Owner expected, Owner actual) {
    	boolean consistent = actual.isEqualTo(expected);
    	if (!consistent) {
    		System.out.println("Inconsistency found between Owners" + "\n" +
    								expected.toString() + " and " + actual.toString());
    		csvOwners.updateOwner(expected, actual);
    	}
    	return new AsyncResult<Boolean>(consistent);
    }


    //Implementation of method to move data from the owners table to a text file
    public void forklift() {
    	String filename ="new-datastore/owners.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from owners";
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
                fw.append(',');
                fw.append(rs.getString(6));
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV file for the owners table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Async
    @Scheduled(fixedDelay = 5000)
    public Future<Integer> checkConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/owners.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from owners";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            for(String[] actual : reader) {
                rs.next();
                for(int i=0;i<6;i++) {
                    int columnIndex = i+1;
                    if(!actual[i].equals(rs.getString(columnIndex))) {
                    	System.out.println("Owners Table Consistency Violation!\n" +
                				"\n\t expected = " + rs.getString(columnIndex)
                				+ "\n\t actual = " + actual[i]);
                        //fix inconsistency
                    	actual[i] = rs.getString(columnIndex);
                    	inconsistencies++;
                    }
                }
            }
            
            if (inconsistencies == 0)
            	System.out.println("No inconsistencies across former owners table dataset.");
            else
            	System.out.println("Number of Inconsistencies for Owners Table: " + inconsistencies);

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return new AsyncResult<Integer>(inconsistencies);
    }


    private int getCSVRow() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/owners.csv"));
    	List<String[]> content = csvReader.readAll();
    	//Returning size + 1 to avoid id of 0
    	csvReader.close();
    	return content.size() + 1;
    }

    public void writeToFile(String firstName, String lastName, String address, String city, String telephone) {
    	String filename ="new-datastore/owners.csv";
        try {

            int ownerId = csvOwners.getCSVRow();

            Owner owner = new Owner();
            owner.setId(ownerId);
            owner.setFirstName(firstName);
            owner.setLastName(lastName);
            owner.setAddress(address);
            owner.setCity(city);
            owner.setTelephone(telephone);
            //this.owners.save(owner);
          
            FileWriter fw = new FileWriter(filename, true);
            writeToMySqlDataBase(owner);

            //Append the new owner to the csv
            fw.append(Integer.toString(ownerId));
            fw.append(',');
            fw.append(firstName);
            fw.append(',');
            fw.append(lastName);
            fw.append(',');
            fw.append(address);
            fw.append(',');
            fw.append(city);
            fw.append(',');
            fw.append(telephone);
            fw.append(',');
            fw.append('\n');
            fw.flush();
            fw.close();

            System.out.println("Shadow write for owner complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String readFromMySqlDataBase(int ownerId) {

    	StringBuilder stringBuilder = new StringBuilder();
    	try {
	        Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
	        String query = "SELECT * FROM owners WHERE id=?";
	        PreparedStatement preparedSelect = conn.prepareStatement(query);
	        preparedSelect.setInt(1, ownerId);

	        ResultSet rs = preparedSelect.executeQuery();

	        while (rs.next()) {
	        	stringBuilder.append(Integer.toString(rs.getInt("id")) + ",");
	        	stringBuilder.append(rs.getString("first_name") + ",");
	        	stringBuilder.append(rs.getString("last_name") + ",");
	        	stringBuilder.append(rs.getString("address") + ",");
	        	stringBuilder.append(rs.getString("city") + ",");
	        	stringBuilder.append(rs.getString("telephone") + ",");
	        }
    	} catch (Exception e) {
            e.printStackTrace();
        }

        String ownerData = stringBuilder.toString();
    	return ownerData;
    }

    public String readFromNewDataStore(int ownerId) {
    	String ownerData = "";

    	try {
    		CSVReader reader = new CSVReader(new FileReader("new-datastore/owners.csv"));

    		for (String[] actual : reader) {
    			if (actual[0].equals(String.valueOf(ownerId))) {
    				for (int i = 0; i < 6; i++) {
    					ownerData += actual[i] + ",";
    				}
    			}
    		}
    		reader.close();

    	} catch (Exception e) {
            e.printStackTrace();
        }
    	return ownerData;
    }
    
    public void writeToMySqlDataBase(Owner owner) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        // the mysql insert statement
        String query = " INSERT into owners (id, first_name, last_name, address, city, telephone)"
          + " Values (?, ?, ?, ?, ?, ?)";

        // Create the MySql insert query
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1, owner.getId());
        preparedStmt.setString(2, owner.getFirstName());
        preparedStmt.setString(3, owner.getLastName());
        preparedStmt.setString(4, owner.getAddress());
        preparedStmt.setString(5, owner.getCity());
        preparedStmt.setString(6, owner.getTelephone());

        // execute the preparedstatement
        preparedStmt.execute();
    }
    

}
