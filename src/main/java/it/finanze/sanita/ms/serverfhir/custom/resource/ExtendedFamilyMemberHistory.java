package it.finanze.sanita.ms.serverfhir.custom.resource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import org.hl7.fhir.r4.model.FamilyMemberHistory;

@ResourceDef(name="FamilyMemberHistory", profile="http://hl7.org/fhir/StructureDefinition/FamilyMemberHistory")
public class ExtendedFamilyMemberHistory extends FamilyMemberHistory {
		
	private static final long serialVersionUID = 2227468313613616903L;

	@SearchParamDefinition(name="author", path="FamilyMemberHistory.note.author")
	public static final String SP_AUTHOR = "author";
	
}
