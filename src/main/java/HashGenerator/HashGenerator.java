package HashGenerator;

import java.security.MessageDigest;

public class HashGenerator {
    public String computeHash(String base) {//hashes a string into HEX SHA-256
      	 try{
      		 	//String base = rs.getArray(columnIndex).toString();
      	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
      	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
      	        StringBuffer hexString = new StringBuffer();

      	        for (int i = 0; i < hash.length; i++) {
      	            String hex = Integer.toHexString(0xff & hash[i]);
      	            if(hex.length() == 1) hexString.append('0');
      	            hexString.append(hex);
      	        }
      	        
      	        return hexString.toString();
      	        
      	    } catch(Exception ex){
      	       throw new RuntimeException(ex);
      	    }
    }
}
