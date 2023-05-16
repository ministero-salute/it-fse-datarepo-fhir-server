package it.finanze.sanita.ms.serverfhir.custom.resource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import org.hl7.fhir.r4.model.Condition;
@ResourceDef(name="Condition", profile="http://hl7.org/fhir/StructureDefinition/Condition")
public class ExtendedCondition extends Condition {
		
	private static final long serialVersionUID = 1117468313613616903L;

	@SearchParamDefinition(name="author", path="Condition.note.author")
	public static final String SP_AUTHOR = "author";
	
}
