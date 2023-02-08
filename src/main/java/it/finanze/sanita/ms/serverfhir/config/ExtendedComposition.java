package it.finanze.sanita.ms.serverfhir.config;

import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Organization;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;

@ResourceDef(name = "Composition", profile = "http://hl7.org/fhir/Profile/Composition")
public class ExtendedComposition extends Composition {
 
	private static final long serialVersionUID = 2760073594700478986L;
	
	@SearchParamDefinition(name="recursive_entry", path="Composition.section.section.entry", description="A reference to data that supports this section", type="reference" )
	public static final String SP_RECURSIVE_ENTRY = "entry";
	
	@SearchParamDefinition(name="custodian", path="Composition.custodian", description="", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Organization") }, target={Organization.class } )
	public static final String SP_CUSTODIAN = "custodian";
	
	@SearchParamDefinition(name="detail", path="Composition.event.detail", description="", type="reference")
	public static final String SP_DETAIL = "detail";
	
}
