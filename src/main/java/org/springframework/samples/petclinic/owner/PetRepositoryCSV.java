package org.springframework.samples.petclinic.owner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

//Pet csv format: Id, Name, Birth date, typeId, OwnerId

public class PetRepositoryCSV implements PetRepository{
	String petFile = "new-datastore/pets.csv";
	String petTypeFile = "new-datastore/pet-types.csv";

	@Override
	public List<PetType> findPetTypes() {
		List<PetType> types = new ArrayList<>();
		CSVReader typeReader = null;

		try {
			typeReader = new CSVReader(new FileReader(petTypeFile));
			String[] row = typeReader.readNext();
			while(row != null) {
				PetType newType = constructType(row);
				types.add(newType);
				row = typeReader.readNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(typeReader != null) {
				try {
					typeReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return types;
	}

	@Override
	public Pet findById(Integer id) {
		CSVReader petReader = null;

		try {
			petReader = new CSVReader(new FileReader(petFile));
			String[] row = petReader.readNext();
			while(row != null) {
				if(row.length > 0 && Integer.parseInt(row[0]) == id) {
					petReader.close();
					return constructPet(row);
				}
				row = petReader.readNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(petReader != null) {
				try {
					petReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	public Pet save(String name, Date birthDate, int typeId, int ownerId) {
		int petId = 0;
		try {
			petId = getCSVRow();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Pet pet = new Pet();
		pet.setId(petId);
		pet.setBirthDate(birthDate);
		pet.setName(name);
		pet.setType(typeId);
		pet.setOwner(ownerId);
		save(pet);
		return pet;
	}

	@Override
	public void save(Pet pet) {
			String filename ="new-datastore/pets.csv";
				try {
						FileWriter fw = new FileWriter(filename, true);

						//Append the new owner to the csv
						fw.append(Integer.toString(petId));
						fw.append(',');
						fw.append(name);
						fw.append(',');
						fw.append(sqlBirthDate.toString());
						fw.append(',');
						fw.append(Integer.toString(typeId));
						fw.append(',');
						fw.append(Integer.toString(ownerId));
						fw.append(',');
						fw.append('\n');
						fw.flush();
						fw.close();

						System.out.println("Shadow write for pet complete.");
				} catch (Exception e) {
						e.printStackTrace();
				}

	}

	private PetType findPetTypeById(Integer id) {
		CSVReader petTypeReader = null;

		try {
			petTypeReader = new CSVReader(new FileReader(petTypeFile));
			String[] row = petTypeReader.readNext();
			while(row != null) {
				if(row.length > 0 && Integer.parseInt(row[0]) == id) {
					petTypeReader.close();
					return constructType(row);
				}
				row = petTypeReader.readNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(petTypeReader != null) {
				try {
					petTypeReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private PetType constructType(String[] row) {
		PetType type = new PetType();
		type.setId(Integer.parseInt(row[0]));
		type.setName(row[1]);

		return type;
	}

	private Pet constructPet(String[] row) {
		Pet newTypet = new Pet();
		newPet.setId(Integer.parseInt(row[0]));
		newPet.setName(row[1]);
		newPet.setBirthDate(new Date(Long.parseLong(row[2])));
		newPet.setType(findPetTypeById(Integer.parseInt(row[3])));
		// Also do something about the owner?????? Problem is a circular dependency

		return new pet;
	}

	public void updatePet(Pet correctPet, Pet petToBeUpdated) {
		CSVReader reader = null;
		try{
			reader = new CSVReader(new FileReader("new-datastore/pets.csv"));

	        for(String[] actual : reader) {
	        	//find the row of the owner to be updated, and update with new owner info
	        	if (actual[0].equals(String.valueOf(correctPet.getId())) ) {
	        		actual[1] = petToBeUpdated.getName();
	        		actual[2] = petToBeUpdated.getBirthDate().toString();
	        		actual[3] = petToBeUpdated.getOwner().toString();
	        		actual[4] = petToBeUpdated.getType().toString();
	        	}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	private int getCSVRow() throws Exception {
		CSVReader csvReader = new CSVReader(new FileReader("new-datastore/pets.csv"));
		List<String[]> content = csvReader.readAll();
		//Returning size + 1 to avoid id of 0
		csvReader.close();
		return content.size() + 1;
	}

}
