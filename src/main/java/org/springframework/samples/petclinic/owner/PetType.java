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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

import com.opencsv.CSVReader;

import HashGenerator.HashGenerator;

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
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/petclinic", "root", "root");
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
    
    public static String appendSQLColumn(int columnNumber) {//concatenates entire column, 1-based index
    	String column = "";
    	List<String> array = new ArrayList();
    	
    	try {
    		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/petclinic?autoReconnect=true&useSSL=false", "root", "root");
        	String query = "select * from types";
        	Statement stmt = conn.createStatement();
    		ResultSet rs = stmt.executeQuery(query);
    		
//    	    while (rs.next()) {
//    	        //column.concat(rs.getString(columnNumber));
//    	        array.add(rs.getString(columnNumber));
//    	    }
//    	    column = array.toString();
    		column = rs.getString(columnNumber);
    	}catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
        return column;
    }
    
    public void hashStorage() {//create csv of hashes
    	String filename ="hash-record/pet-types.csv";
        try {
            FileWriter fw = new FileWriter(filename);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            HashGenerator hash = new HashGenerator();
    		String sqlColumn;
    		String sqlColumn2;

    		sqlColumn = appendSQLColumn(1);
			sqlColumn = hash.computeHash(sqlColumn);
			fw.append(sqlColumn);
			fw.append(',');
			sqlColumn2 = appendSQLColumn(2);
			sqlColumn2 = hash.computeHash(sqlColumn);
			fw.append(sqlColumn2);
			fw.append('\n');

            fw.flush();
            fw.close();
            System.out.println("CSV file for the pets table has been created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public int repurposedCheckConsistency() {//comparing hash of columns
    	int inconsistencies = 0;
    	HashGenerator hash = new HashGenerator();
		
		try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/pet-types.csv"));
			
			for(int i=0;i<2;i++) {
		    	String sqlColumn = "";
		    	String csvColumn = "";
		    	
		    	sqlColumn = appendSQLColumn(i+1);
				
				for(String[] actual : reader) {//appends entire csv column, 0-indexed
					csvColumn.concat(actual[i]);
				}
				
				sqlColumn = hash.computeHash(sqlColumn);//store after computing hash
				csvColumn = hash.computeHash(csvColumn);
	    	
		    	if(!sqlColumn.equals(csvColumn)) {
		    		System.out.println("Consistency Violation!\n" + 
		    				"\n\t changes changes found in column "+i+" of CSV");
		    		inconsistencies++;
		    	}
			}
			if (inconsistencies == 0) 
            	System.out.println("No inconsistencies across former types table dataset.");
			
    	}catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
		return inconsistencies;
    }

    public int checkConsistency() {
        int inconsistencies = 0;

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/pet-types.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/petclinic", "root", "root");
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
    
    public static void main(String args[]){
    	HashGenerator h = new HashGenerator();
        PetType pt = new PetType();

        try {
            CSVReader reader = new CSVReader(new FileReader("new-datastore/pet-types.csv"));

            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/petclinic?autoReconnect=true&useSSL=false", "root", "root");
            String query = "select * from types";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            rs.next();
            System.out.println(rs.getString(1));

        }catch(Exception e) {
            System.out.print("Error " + e.getMessage());
        }
    	
    	
//    	System.out.println(appendSQLColumn(1));
//    	System.out.println(appendSQLColumn(2));
//    	
//    	System.out.println(h.computeHash(appendSQLColumn(1)));
//    	System.out.println(h.computeHash(appendSQLColumn(2)));
//    	
//    	System.out.println(pt.repurposedCheckConsistency());
//    	pt.repurposedCheckConsistency();
//    	pt.hashStorage();
    	
    }
}
