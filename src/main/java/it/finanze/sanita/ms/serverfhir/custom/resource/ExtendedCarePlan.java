package it.finanze.sanita.ms.serverfhir.custom.resource;

import org.hl7.fhir.r4.model.CarePlan;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;

@ResourceDef(name="CarePlan", profile="http://hl7.org/fhir/StructureDefinition/CarePlan")
public class ExtendedCarePlan extends CarePlan {
	
	private static final long serialVersionUID = 6356702736973254484L;
	
	@SearchParamDefinition(name="reasonReference", path="CarePlan.activity.detail.reasonReference", description="", type="reference")
	public static final String SP_REASON_REFERENCE = "reasonReference";

	@SearchParamDefinition(name="outcomeReference", path="CarePlan.activity.outcomeReference", description="", type="reference")
	public static final String SP_OUTCOME_REFERENCE = "outcomeReference";

}