package it.finanze.sanita.ms.serverfhir.config;

import org.hl7.fhir.r4.model.Procedure;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;

@ResourceDef(name="Procedure", profile="http://hl7.org/fhir/StructureDefinition/Procedure")
public class ExtendedProcedure extends Procedure {
		
	private static final long serialVersionUID = -1995633819405471724L;
	
	@SearchParamDefinition(name="usedReference", path="Procedure.usedReference", description="", type="reference")
	public static final String SP_USED_REFERENCE = "usedReference";
	
}
