package it.finanze.sanita.ms.serverfhir.custom.auth;

import java.util.Base64;

import ca.uhn.fhir.rest.api.server.RequestDetails;

public class BasicAuthUtility {

	private BasicAuthUtility() {
		//This method is leave intentionally void.
	}
	
	public static BasicAuthDTO calculate(RequestDetails theRequestDetails) {
		BasicAuthDTO out = new BasicAuthDTO();
		String authHeader = theRequestDetails.getHeader("Authorization");
		if (authHeader!=null && authHeader.length()>0) {
			String[] tokens = authHeader.split(" ");
			if (tokens[0].equalsIgnoreCase("Basic")) {
				String decodedString = decodeBase64(tokens[1]);
				tokens = decodedString.split(":");
				String usr = tokens[0];
				out.setUser(usr);
				String pwd = tokens[1];
				out.setPassword(pwd);
			}
		}
		return out;
	}

	private static String decodeBase64(String encodedString) {
		byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
		return new String(decodedBytes);
	}

}
