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

import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.opencsv.CSVReader;
import org.springframework.samples.petclinic.model.NamedEntity;

/**
 * @author Juergen Hoeller
 *         Can be Cat, Dog, Hamster...
 */
@Entity
@Table(name = "types")
public class PetType extends NamedEntity {
	//Implementation of method to move data from pet types table to csv file
    public void forklift() {
    	String filename ="new-datastore/pet-types.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "penis");
            String query = "select * from types";
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
            System.out.println("CSV file for the types table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int checkConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/pet-types.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "penis");
            String query = "select * from types";
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
            	System.out.println("No inconsistencies across former types table dataset.");
            else
            	System.out.println("Number of Inconsistencies: " + inconsistencies);
            
        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return inconsistencies;
    }
    
    public void writeToMySqlDataBase(String name) throws Exception {

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        String query = " INSERT into types (name)"
          + " Values (?)";

        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, name);

        // execute the preparedstatement
        preparedStmt.execute();
    }

    public void writeToFile(String name) {
    	String filename ="new-datastore/pet-types.csv";
        try {
            FileWriter fw = new FileWriter(filename, true);

            writeToMySqlDataBase(name);
            String typeId = retrieveIdOfTypeFromDb(name);

            //Append the new type to the csv
            fw.append(typeId);
            fw.append(',');
            fw.append(name);
            fw.append('\n');
            fw.flush();
            fw.close();

            System.out.println("Shadow write for types complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String retrieveIdOfTypeFromDb(String name) throws Exception{

    	Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/petclinic", "root", "root");

        String selectQuery= "SELECT id FROM types WHERE name=?";

        PreparedStatement preparedSelect = conn.prepareStatement(selectQuery);
        preparedSelect.setString(1, name);

        ResultSet rs = preparedSelect.executeQuery();
        if(rs.next()){
        	return Integer.toString(rs.getInt("id"));
        }
        return null;
    }

}
