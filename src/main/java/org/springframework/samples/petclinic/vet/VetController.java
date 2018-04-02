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
package org.springframework.samples.petclinic.vet;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
public
class VetController {

    private final VetRepository vets;

    @Autowired
    public VetController(VetRepository clinicService) {
        this.vets = clinicService;
    }

    @GetMapping("/vets.html")
    public String showVetList(Map<String, Object> model) {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for Object-Xml mapping
        Vets vets = new Vets();
        vets.getVetList().addAll(this.vets.findAll());
        model.put("vets", vets);
        return "vets/vetList";
    }

    @GetMapping({ "/vets.json", "/vets.xml" })
    public @ResponseBody Vets showResourcesVetList() {
        // Here we are returning an object of type 'Vets' rather than a collection of Vet
        // objects so it is simpler for JSon/Object mapping
        Vets vets = new Vets();
        vets.getVetList().addAll(this.vets.findAll());
        return vets;
    }

    //Implementation of method to move data from vets table to csv file
    public void forklift() {
    	String filename ="new-datastore/vets.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from vets";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                fw.append(rs.getString(1));
                fw.append(',');
                fw.append(rs.getString(2));
                fw.append(',');
                fw.append(rs.getString(3));
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            conn.close();
            System.out.println("CSV file for the vets table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int checkConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/vets.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from vets";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            for(String[] actual : reader) {
                rs.next();
                for(int i=0;i<3;i++) {
                    int columnIndex = i+1;
                    if(!actual[i].equals(rs.getString(columnIndex))) {
                    	System.out.println("Consistency Violation!\n" +
                				"\n\t expected = " + rs.getString(columnIndex)
                				+ "\n\t actual = " + actual[i]);
                    	//fix inconsistency
                    	actual[i] = rs.getString(columnIndex);
                        inconsistencies++;
                    }
                }
            }

            if (inconsistencies == 0)
            	System.out.println("No inconsistencies across former vets table dataset.");
            else
            	System.out.println("Number of Inconsistencies: " + inconsistencies);

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return inconsistencies;
    }

    public void writeToMySqlDataBase(int vetId, String firstName, String lastName) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        // the mysql insert statement
        String query = " INSERT into vets (id, first_name, last_name)"
          + " Values (?, ?, ?)";

        // Create the MySql insert query
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1, vetId);
        preparedStmt.setString(2, firstName);
        preparedStmt.setString(3, lastName);

        // execute the prepared statement
        preparedStmt.execute();
    }

    public void writeToFile(String firstName, String lastName) {
		String filename = "new-datastore/vets.csv";
		try {
			int vetId = getCSVRow();
			FileWriter fw = new FileWriter(filename, true);

			writeToMySqlDataBase(vetId, firstName, lastName);

			// Append the new owner to the csv
			fw.append(Integer.toString(vetId));
			fw.append(',');
			fw.append(firstName);
			fw.append(',');
			fw.append(lastName);
			fw.append('\n');
			fw.flush();
			fw.close();

			System.out.println("Shadow write complete for vets.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  public String readFromMySqlDataBase(int vetId) {

      StringBuilder stringBuilder = new StringBuilder();
      try {
          Class.forName("com.mysql.jdbc.Driver").newInstance();
          Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
          String query = "SELECT * FROM vets WHERE id=?";
          PreparedStatement preparedSelect = conn.prepareStatement(query);
          preparedSelect.setInt(1, vetId);

          ResultSet rs = preparedSelect.executeQuery();

          while (rs.next()) {
            stringBuilder.append(Integer.toString(rs.getInt("id")) + ",");
  			stringBuilder.append(rs.getString("first_name") + ",");
            stringBuilder.append(rs.getString("last_name") + ",");
          }
      } catch (Exception e) {
            e.printStackTrace();
        }

        String vetData = stringBuilder.toString();
      return vetData;
    }

    public String readFromNewDataStore(int vetId) {
      String vetData = "";

      try {
        CSVReader reader = new CSVReader(new FileReader("new-datastore/vets.csv"));

        for (String[] actual : reader) {
          if (actual[0].equals(String.valueOf(vetId))) {
            for (int i = 0; i < 3; i++) {
              vetData += actual[i] + ",";
            }
          }
        }

      } catch (Exception e) {
            e.printStackTrace();
        }

      return vetData;
    }
    
    private int getCSVRow() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/vets.csv"));
    	List<String[]> content = csvReader.readAll();
    	//Returning size + 1 to avoid id of 0
    	csvReader.close();
    	return content.size() + 1;
    }
}
