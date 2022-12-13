package it.finanze.sanita.ms.serverfhir.custom.crypt;

import java.util.Base64;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;

import com.voltage.securedata.enterprise.AES;
import com.voltage.securedata.enterprise.VeException;

import ca.uhn.fhir.jpa.dao.GZipUtil;
import ca.uhn.fhir.jpa.model.entity.ResourceHistoryTable;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import it.finanze.sanita.ms.serverfhir.custom.helper.FHIRR4Helper;

public class CryptUtility {

	private CryptUtility() {
		//This method is leave intentionally void.
	}

	public static void decrypt(ResponseDetails theResponseDetails, AES aes) {
		if (theResponseDetails.getResponseResource().getClass().equals(Bundle.class)) {
			Bundle bundle = (Bundle) theResponseDetails.getResponseResource();
			if (BundleType.SEARCHSET.equals(bundle.getType())) {
				for (BundleEntryComponent bec:bundle.getEntry()) {
					Resource r = bec.getResource();
					if (ResourceType.Patient.equals(r.getResourceType())) {
						Patient patient = (Patient) r;
						if (aes == null) {
							decodeInfoPatient(patient, aes);
						}
					}
				}
			}
		}
	}

	public static void decrypt(ResourceHistoryTable rht, AES aes) {
		if(ResourceType.Patient.equals(ResourceType.fromCode(rht.getResourceType()))) {
			Patient patient = FHIRR4Helper.deserializeResource(Patient.class, GZipUtil.decompress(rht.getResource()), true);
		    decodeInfoPatient(patient, aes);
		}
	}

	private static void decodeInfoPatient(Patient patient, AES aes) {
		for(HumanName humanName : patient.getName()) {
			humanName.setFamily(decodeAES(humanName.getFamily(), aes));
			for(StringType given : humanName.getGiven()) {
				given.setValue(decodeAES(given.getValue(), aes));
			}
		}
		for(Identifier id : patient.getIdentifier()) {
			id.setValue(decodeAES(id.getValue(), aes));
		}
	}

	private static String decodeAES(String str, AES aes) {
		try {
			byte[] resultBytes = aes.access(Base64.getDecoder().decode(str));
			return new String(resultBytes);
		} catch (VeException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] encrypt(byte[] resource, AES aes) {
		Patient patient = FHIRR4Helper.deserializeResource(Patient.class, GZipUtil.decompress(resource), true);

		for(HumanName humanName : patient.getName()) {
			humanName.setFamily(encodeAES(humanName.getFamily(), aes));
			for(StringType given : humanName.getGiven()) {
				given.setValue(encodeAES(given.getValue(), aes));
			}
		}
		for(Identifier id : patient.getIdentifier()) {
			id.setValue(encodeAES(id.getValue(), aes));
		}

		return GZipUtil.compress(FHIRR4Helper.serializeResource(patient, false, false, false));
	}

	private static String encodeAES(String str, AES aes) {
		String out = null;
		if (str != null) {
			try {
		        byte[] ciphertextBytes = aes.protect(str.getBytes());
		        out = Base64.getEncoder().encodeToString(ciphertextBytes);
			} catch (VeException e) {
				throw new RuntimeException(e);
			}
		}
		return out;
	}

	/*
	public static void decrypt(ResourceHistoryTable rht, AES aes) {
		if(ResourceType.Patient.equals(ResourceType.fromCode(rht.getResourceType()))) {
			if (aes==null) {
				rht.setResource(reverseInfoPatient(rht.getResource()));
			} else {
				Patient patient = FHIRR4Helper.deserializeResource(Patient.class, GZipUtil.decompress(rht.getResource()), true);
			    decodeInfoPatient(patient, aes);
			}
		}
	}

	private static String decodeAES(String str, AES aes) {
		try {
			byte[] resultBytes = aes.access(Base64.getDecoder().decode(str));
			return new String(resultBytes);
		} catch (VeException e) {
			throw new RuntimeException(e);
		}
	}

	public static void decrypt(ResponseDetails theResponseDetails, AES aes) {
		if (theResponseDetails.getResponseResource().getClass().equals(Bundle.class)) {
			Bundle bundle = (Bundle) theResponseDetails.getResponseResource();
			if (BundleType.SEARCHSET.equals(bundle.getType())) {
				for (BundleEntryComponent bec:bundle.getEntry()) {
					Resource r = bec.getResource();
					if (ResourceType.Patient.equals(r.getResourceType())) {
						Patient patient = (Patient) r;
						if (aes == null) {
							reverseInfoPatient(patient);
						} else {
							decodeInfoPatient(patient, aes);
						}
					}
				}
			}
		}
	}

	public static byte[] reverseInfoPatient(byte[] resource) {
		byte[] out = null;
		try {
			Patient patient = FHIRR4Helper.deserializeResource(Patient.class, GZipUtil.decompress(resource), true);

			reverseInfoPatient(patient);
			out = GZipUtil.compress(FHIRR4Helper.serializeResource(patient, false, false, false));
			
		} catch(Exception ex) {
			System.out.println("Eccezione : " + ex);
		}
		return out;
	}

	private static void reverseInfoPatient(Patient patient) {
		for(HumanName humanName : patient.getName()) {
			humanName.setFamily(StringUtils.reverse(humanName.getFamily()));
			for(StringType given : humanName.getGiven()) {
				given.setValue(StringUtils.reverse(given.getValue()));
			}
		}
		for(Identifier id : patient.getIdentifier()) {
			id.setValue(StringUtils.reverse(id.getValue()));
		}
	}

	private static void decodeInfoPatient(Patient patient, AES aes) {
		for(HumanName humanName : patient.getName()) {
			humanName.setFamily(decodeAES(humanName.getFamily(), aes));
			for(StringType given : humanName.getGiven()) {
				given.setValue(decodeAES(given.getValue(), aes));
			}
		}
		for(Identifier id : patient.getIdentifier()) {
			id.setValue(decodeAES(id.getValue(), aes));
		}
	}
*/
}
