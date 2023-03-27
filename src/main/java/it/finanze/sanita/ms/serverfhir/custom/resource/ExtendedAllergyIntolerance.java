package it.finanze.sanita.ms.serverfhir.custom.resource;

import org.hl7.fhir.r4.model.AllergyIntolerance;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;

@ResourceDef(name="AllergyIntolerance", profile="http://hl7.org/fhir/StructureDefinition/AllergyIntolerance")
public class ExtendedAllergyIntolerance extends AllergyIntolerance {
	
	private static final long serialVersionUID = -4071113804800800402L;

	@SearchParamDefinition(name="encounter", path="AllergyIntolerance.encounter", description="", type="reference")
	public static final String SP_ENCOUNTER = "encounter";

}