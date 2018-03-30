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
import java.util.Map;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private final OwnerRepository owners;


    @Autowired
    public OwnerController(OwnerRepository clinicService) {
        this.owners = clinicService;
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
        mav.addObject(this.owners.findById(ownerId));
        return mav;
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

    public int checkConsistency() {
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
                    	System.out.println("Consistency Violation!\n" +
                				"\n\t expected = " + rs.getString(columnIndex)
                				+ "\n\t actual = " + actual[i]);
                        inconsistencies++;
                    }
                }
            }

            if (inconsistencies == 0)
            	System.out.println("No inconsistencies across former owners table dataset.");
            else
            	System.out.println("Number of Inconsistencies: " + inconsistencies);

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return inconsistencies;
    }

    public void writeToMySqlDataBase(String firstName, String lastName, String address, 
    		String city, String telephone) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        // the mysql insert statement
        String query = " INSERT into owners (first_name, last_name, address, city, telephone)"
          + " Values (?, ?, ?, ?, ?)";

        // Create the MySql insert query
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, firstName);
        preparedStmt.setString(2, lastName);
        preparedStmt.setString(3, address);
        preparedStmt.setString(4, city);
        preparedStmt.setString(5, telephone);

        // execute the preparedstatement
        preparedStmt.execute();
    }

    public void writeToFile(String firstName, String lastName, String address, String city, String telephone) {
    	String filename ="new-datastore/owners.csv";
        try {
            FileWriter fw = new FileWriter(filename, true);

            writeToMySqlDataBase(firstName, lastName, address, city, telephone);
            String ownerId = retrieveIdOfOwnerFromDb(firstName, lastName, address, city, telephone);

            //Append the new owner to the csv
            fw.append(ownerId);
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

            System.out.println("Shadow write complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String retrieveIdOfOwnerFromDb(String firstName, String lastName, 
    										String address, String city, String telephone) throws Exception{
    	
    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
        //Retrieve the id created
        String selectQuery= "SELECT id FROM owners WHERE first_name=?" +
        					" AND last_name=? AND address=? AND city=? AND telephone=?";

        PreparedStatement preparedSelect = conn.prepareStatement(selectQuery);
        preparedSelect.setString(1, firstName);
        preparedSelect.setString(2, lastName);
        preparedSelect.setString(3, address);
        preparedSelect.setString(4, city);
        preparedSelect.setString(5, telephone);

        ResultSet rs = preparedSelect.executeQuery();
        if(rs.next()){
        	return Integer.toString(rs.getInt("id"));
        }
        return null;
    }

}

