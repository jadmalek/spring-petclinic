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

import java.io.FileWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.persistence.Entity;
import javax.persistence.Table;

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

}
