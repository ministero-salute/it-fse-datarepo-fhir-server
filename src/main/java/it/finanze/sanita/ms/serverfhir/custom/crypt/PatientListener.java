package it.finanze.sanita.ms.serverfhir.custom.crypt;

import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hl7.fhir.r4.model.ResourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ca.uhn.fhir.jpa.model.entity.ResourceHistoryTable;

public class PatientListener {
	
	@Value("${crypt.status}")
	private Boolean cryptStatus;

	@Autowired
	private FPEFactory fpeFactory;
	
	@PostLoad
	@PostUpdate
	//@PostPersist
	public void decrypt(Object obj) {
		if (cryptStatus!=null && cryptStatus) {
			ResourceHistoryTable rht = interceptResourcePatient(obj);
			if (rht != null) {
				CryptUtility.decrypt(rht, fpeFactory.getAES());
			}
		}
	}

	@PrePersist
	@PreUpdate
	public void encrypt(Object obj) {
		if (cryptStatus!=null && cryptStatus) {
			ResourceHistoryTable rht = interceptResourcePatient(obj);
			if (rht!=null) {
				encrypt(rht);
			}
		}
	}

	private void encrypt(ResourceHistoryTable rht) {
		if(ResourceType.Patient.equals(ResourceType.fromCode(rht.getResourceType()))) {
			rht.setResource(CryptUtility.encrypt(rht.getResource(), fpeFactory.getAES()));
		}
	}

	private ResourceHistoryTable interceptResourcePatient(Object pc) {
		ResourceHistoryTable out = null;
		if (pc instanceof ResourceHistoryTable) {
			out = (ResourceHistoryTable) pc;
		}
		return out;
	}

	/*

	private void decrypt(ResourceHistoryTable rht) {
		if(ResourceType.Patient.equals(ResourceType.fromCode(rht.getResourceType()))) {
			rht.setResource(reverseInfoPatient(rht.getResource()));
		}
		if (cryptDebug!=null && cryptDebug) {
			dumpResource(rht);
		}
	}

	private static void dumpResource(ResourceHistoryTable rht) {
		System.out.println("TESTO DECRIPTATO: " + GZipUtil.decompress(rht.getResource()));
	}

	private byte[] reverseInfoPatient(byte[] resource) {
		byte[] out = null;
		try {
			Patient patient = FHIRR4Helper.deserializeResource(Patient.class, GZipUtil.decompress(resource), true);

			for(HumanName humanName : patient.getName()) {
				humanName.setFamily(StringUtils.reverse(humanName.getFamily()));
				for(StringType given : humanName.getGiven()) {
					given.setValue(StringUtils.reverse(given.getValue()));
				}
			}
			for(Identifier id : patient.getIdentifier()) {
				id.setValue(StringUtils.reverse(id.getValue()));
			}
			out = GZipUtil.compress(FHIRR4Helper.serializeResource(patient, false, false, false));
			
		} catch(Exception ex) {
			System.out.println("Eccezione : " + ex);
		}
		return out;
	}
*/
}