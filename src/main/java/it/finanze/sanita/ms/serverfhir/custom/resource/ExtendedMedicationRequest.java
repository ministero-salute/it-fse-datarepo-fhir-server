package it.finanze.sanita.ms.serverfhir.custom.resource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import org.hl7.fhir.r4.model.MedicationRequest;

@ResourceDef(name="MedicationRequest", profile="http://hl7.org/fhir/StructureDefinition/MedicationRequest")
public class ExtendedMedicationRequest extends MedicationRequest {
		
	private static final long serialVersionUID = 5557468313613616903L;

	@SearchParamDefinition(name="author", path="MedicationRequest.note.author")
	public static final String SP_AUTHOR = "author";
	
}
