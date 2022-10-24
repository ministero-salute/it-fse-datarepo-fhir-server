package it.finanze.sanita.ms.serverfhir.repository;

import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;

public interface IRepositoryValidationInterceptorFactory {
	RepositoryValidatingInterceptor buildUsingStoredStructureDefinitions();

	RepositoryValidatingInterceptor build();
}
