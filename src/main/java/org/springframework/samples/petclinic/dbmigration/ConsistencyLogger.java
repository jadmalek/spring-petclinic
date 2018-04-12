package org.springframework.samples.petclinic.dbmigration;

public class ConsistencyLogger {
	private static int total = 0;
	private static int failures = 0;
	private static final double THRESHOLD = 0.01;
	private static boolean shouldSwitch = false;
	
	public static void logCheck() {
		total++;
	}
	
	public static void logInconsistent() {
		total++;
	}
	
	public static boolean shouldSwitchDatastore() {
		if(!shouldSwitch && total > 1000 && failures/total < THRESHOLD) {
			//trigger the flag switch
			shouldSwitch = true;
		}
		
		return shouldSwitch;
	}
}
