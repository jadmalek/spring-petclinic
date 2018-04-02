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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.opencsv.CSVReader;
import org.springframework.samples.petclinic.model.NamedEntity;

/**
 * Models a {@link Vet Vet's} specialty (for example, dentistry).
 *
 * @author Juergen Hoeller
 */
@Entity
@Table(name = "specialties")
public class Specialty extends NamedEntity implements Serializable {
	//Implementation of method to move data from the specialties table to csv file
	public void forkliftSpecialties() {
		String filename ="new-datastore/specialties.csv";
		try {
			FileWriter fw = new FileWriter(filename);
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
			String query = "select * from specialties";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				fw.append(rs.getString(1));
				fw.append(',');
				fw.append(rs.getString(2));
				fw.append('\n');
			}
			fw.flush();
			fw.close();
			conn.close();
			System.out.println("CSV file for the specialties table has been created successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public int checkSpecialtiesConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/specialties.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from specialties";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            for(String[] actual : reader) {
                rs.next();
                for(int i=0;i<2;i++) {
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
            	System.out.println("No inconsistencies across former specialties table dataset.");
            else
            	System.out.println("Number of Inconsistencies: " + inconsistencies);

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return inconsistencies;
    }

	//Implementation of method to move data from the vet_specialties table to a csv file
	public void forkliftVetSpecialties() {
		String filename ="new-datastore/vet-specialties.csv";
		try {
			FileWriter fw = new FileWriter(filename);
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
			String query = "select * from vet_specialties";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				fw.append(rs.getString(1));
				fw.append(',');
				fw.append(rs.getString(2));
				fw.append('\n');
			}
			fw.flush();
			fw.close();
			conn.close();
			System.out.println("CSV file for the vet_specialties table has been created successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public int checkVetSpecialtiesConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/vet-specialties.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
            String query = "select * from vet_specialties";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            for(String[] actual : reader) {
                rs.next();
                for(int i=0;i<2;i++) {
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
            	System.out.println("No inconsistencies across former vet_specialties table dataset.");
            else
            	System.out.println("Number of Inconsistencies: " + inconsistencies);

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return inconsistencies;
    }

    public void writeToMySqlDataBaseSpecialties(int specialtyId, String name) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        // the mysql insert statement
        String query = " INSERT into specialties (id, name)"
          + " Values (?, ?)";

        // Create the MySql insert query
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1, specialtyId);
        preparedStmt.setString(2,  name);

        // execute the prepared statement
        preparedStmt.execute();
    }

    public void writeToMySqlDataBaseVetSpecialties(int vetId, int specialtyID) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        // the mysql insert statement
        String query = " INSERT into vet_specialties (vet_id, specialty_id)"
          + " Values (?, ?)";

        // Create the MySql insert query
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setInt(1, vetId);
        preparedStmt.setInt(2, specialtyID);

        // execute the prepared statement
        preparedStmt.execute();
    }

	public void writeToFileSpecialties(String name) {
		String filename = "new-datastore/specialties.csv";
		try {
			int specialtyId = getCSVRowForSpecialty();
			FileWriter fw = new FileWriter(filename, true);

			writeToMySqlDataBaseSpecialties(specialtyId, name);

			// Append the new owner to the csv
			fw.append(Integer.toString(specialtyId));
			fw.append(',');
			fw.append(name);
			fw.append(',');
			fw.append('\n');
			fw.flush();
			fw.close();

			System.out.println("Shadow write complete for vet specialties.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeToFileVetSpecialties(int vetId, int specialityId) {
		String filename = "new-datastore/vet-specialties.csv";
		try {
			FileWriter fw = new FileWriter(filename, true);

			writeToMySqlDataBaseVetSpecialties(vetId, specialityId);

			// Append the new vet-specialty to the csv
			fw.append(Integer.toString(vetId));
			fw.append(',');
			fw.append(Integer.toString(specialityId));

			fw.append('\n');
			fw.flush();
			fw.close();

			System.out.println("Shadow write complete for vet specialties.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    private int getCSVRowForSpecialty() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/specialties.csv"));
    	List<String[]> content = csvReader.readAll();
    	csvReader.close();
    	//Returning size + 1 to avoid id of 0
    	return content.size() + 1;
    }
    
    private int getCSVRowForVetSpecialty() throws Exception {
    	CSVReader csvReader = new CSVReader(new FileReader("new-datastore/vet-specialties.csv"));
    	List<String[]> content = csvReader.readAll();
    	csvReader.close();
    	//Returning size + 1 to avoid id of 0
    	return content.size() + 1;
    }
    

	public String readFromMySqlDataBaseSpecialties(int specialtyId) {

		StringBuilder stringBuilder = new StringBuilder();
		try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");
				String query = "SELECT * FROM specialties WHERE id=?";
				PreparedStatement preparedSelect = conn.prepareStatement(query);
				preparedSelect.setInt(1, specialtyId);

				ResultSet rs = preparedSelect.executeQuery();

				while (rs.next()) {
					stringBuilder.append(Integer.toString(rs.getInt("id")) + ",");
					stringBuilder.append(rs.getString("name") + ",");
				}
		} catch (Exception e) {
					e.printStackTrace();
			}

			String specialtyData = stringBuilder.toString();
		return specialtyData;
	}

	public String readFromNewDataStore(int specialtyId) {
		String specialtyData = "";

		try {
			CSVReader reader = new CSVReader(new FileReader("new-datastore/specialties.csv"));

			for (String[] actual : reader) {
				if (actual[0].equals(String.valueOf(specialtyId))) {
					for (int i = 0; i < 2; i++) {
						specialtyData += actual[i] + ",";
					}
				}
			}
			reader.close();

		} catch (Exception e) {
					e.printStackTrace();
			}

		return specialtyData;
	}

}
