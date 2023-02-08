package it.finanze.sanita.ms.serverfhir.config;

import org.hl7.fhir.r4.model.DiagnosticReport;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;

@ResourceDef(name="DiagnosticReport", profile="http://hl7.org/fhir/StructureDefinition/DiagnosticReport")
public class ExtendedDiagnosticReport extends DiagnosticReport {
		
	private static final long serialVersionUID = 6647468313613616903L;

	@SearchParamDefinition(name="imagingStudy", path="DiagnosticReport.imagingStudy", description="", type="reference")
	public static final String SP_IMAGING_STUDY = "imagingStudy";
	
}
