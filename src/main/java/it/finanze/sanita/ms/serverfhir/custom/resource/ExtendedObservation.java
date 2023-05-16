package it.finanze.sanita.ms.serverfhir.custom.resource;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import org.hl7.fhir.r4.model.Observation;

@ResourceDef(name="Observation", profile="http://hl7.org/fhir/StructureDefinition/Observation")
public class ExtendedObservation extends Observation {
		
	private static final long serialVersionUID = 3337468313613616903L;

	@SearchParamDefinition(name="author", path="Observation.note.author")
	public static final String SP_AUTHOR = "author";
	
}
