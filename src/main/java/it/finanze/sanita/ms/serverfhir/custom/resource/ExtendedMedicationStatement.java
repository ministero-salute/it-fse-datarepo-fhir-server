package it.finanze.sanita.ms.serverfhir.custom.resource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import org.hl7.fhir.r4.model.MedicationStatement;

@ResourceDef(name="MedicationStatement", profile="http://hl7.org/fhir/StructureDefinition/MedicationStatement")
public class ExtendedMedicationStatement extends MedicationStatement {
		
	private static final long serialVersionUID = 5257468313613616903L;

	@SearchParamDefinition(name="author", path="MedicationStatement.note.author")
	public static final String SP_AUTHOR = "author";
	
}
