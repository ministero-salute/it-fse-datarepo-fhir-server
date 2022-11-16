package it.finanze.sanita.ms.serverfhir.config;

import org.hl7.fhir.r4.model.Composition;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;

@ResourceDef(name = "Composition", profile = "http://hl7.org/fhir/Profile/Composition")
public class RecursiveComposition extends Composition {
 
	@SearchParamDefinition(name="recursive_entry", path="Composition.section.section.entry", description="A reference to data that supports this section", type="reference" )
	public static final String SP_RECURSIVE_ENTRY = "entry";

	public static String getSpRecursiveEntry() {
		return SP_RECURSIVE_ENTRY;
	}
	
	
}
