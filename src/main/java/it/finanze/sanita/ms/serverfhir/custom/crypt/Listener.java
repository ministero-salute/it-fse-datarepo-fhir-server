package it.finanze.sanita.ms.serverfhir.custom.crypt;

import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.dao.GZipUtil;
import ca.uhn.fhir.jpa.model.entity.ResourceHistoryTable;

@Component
public class Listener {

	private static FhirContext fhirContextR4;

	static {
		fhirContextR4 = FhirContext.forR4();
	}

	@Value("${crypt.status}")
	private Boolean cryptStatus;

	@PostLoad
	@PostUpdate
	public void decrypt(Object pc) {
		try {
			if(Boolean.TRUE.equals(cryptStatus)) {
				if (!(pc instanceof ResourceHistoryTable)) {
					return;
				}
				ResourceHistoryTable rt = (ResourceHistoryTable) pc;
				if(ResourceType.Patient.equals(ResourceType.fromCode(rt.getResourceType()))) {
					//decryptPatient
					rt.setResource(reverseInfoPatient(rt.getResource()));
				}  
			}
		} catch (Exception ex) {
			System.out.println("Eccezione : " + ex);
		}
	}

	@PrePersist
	@PreUpdate
	public void encrypt(Object pc) {
		if(Boolean.TRUE.equals(cryptStatus)) {
			if (!(pc instanceof ResourceHistoryTable)) {
				return;
			}
			ResourceHistoryTable rt = (ResourceHistoryTable) pc;
			if(ResourceType.Patient.equals(ResourceType.fromCode(rt.getResourceType()))) {
				//encryptPatient
				rt.setResource(reverseInfoPatient(rt.getResource()));
			} else if(ResourceType.Bundle.equals(ResourceType.fromCode(rt.getResourceType()))) {
				System.out.println("Sono un bundle");
			}
		}

	}

	private byte[] reverseInfoPatient(byte[] resource) {
		byte[] out = null;
		Patient patient = fhirContextR4.newJsonParser().parseResource(Patient.class, GZipUtil.decompress(resource));
		for(HumanName humanName : patient.getName()) {
			humanName.setFamily(StringUtils.reverse(humanName.getFamily()));
			for(StringType given : humanName.getGiven()) {
				given.setValue(StringUtils.reverse(given.getValue()));
			}
		}

		out = GZipUtil.compress(fhirContextR4.newJsonParser().encodeResourceToString(patient)); 

		return out;
	}
}