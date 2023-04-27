package it.finanze.sanita.ms.serverfhir.custom.capabilities;

import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.util.FhirTerser;
import it.finanze.sanita.ms.serverfhir.custom.helper.SearchParamHelper;
import it.finanze.sanita.ms.serverfhir.custom.resource.ExtendedCapabilityStatement;
import it.finanze.sanita.ms.serverfhir.custom.resource.ExtendedCapabilityStatement.CustomCapabilityStatementRestResourceComponent;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.StringType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JpaCapabilityStatementCustomProvider extends JpaCapabilityStatementProvider {


	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JpaCapabilityStatementCustomProvider.class);

	private final ISearchParamRegistry registry;

	private final List<CustomCapabilityStatementRestResourceComponent> searches;

	public JpaCapabilityStatementCustomProvider(RestfulServer theRestfulServer, IFhirSystemDao<?, ?> theSystemDao, DaoConfig theDaoConfig, ISearchParamRegistry theSearchParamRegistry, IValidationSupport theValidationSupport) {
		super(theRestfulServer, theSystemDao, theDaoConfig, theSearchParamRegistry, theValidationSupport);
		this.registry = theSearchParamRegistry;
		this.searches = new ArrayList<>();
	}

	protected void computeSearchParams(FhirTerser terser, IBase resource, String name) {
		HashMap<String, RuntimeSearchParam> searchParams = new HashMap<>(registry.getActiveSearchParams(name));
		List<String> allPaths = SearchParamHelper.getAllPaths(searchParams, name);
		List<String> splitPaths = SearchParamHelper.splitPathsWithPipe(allPaths, name);
		List<String> purePaths = SearchParamHelper.getPurePaths(splitPaths);
		List<String> dirtyPaths = SearchParamHelper.getDirtyPaths(splitPaths);
		List<String> cleanedPaths = SearchParamHelper.cleanPaths(name, dirtyPaths);
		List<StringType> result = SearchParamHelper.aggregate(purePaths, cleanedPaths);
		this.searches.add(new CustomCapabilityStatementRestResourceComponent(name, result));
	}

	@Override
	protected void postProcessRestResource(FhirTerser terser, IBase res, String name) {
		super.postProcessRestResource(terser, res, name);
		computeSearchParams(terser, res, name);
	}

	@Override
	protected void postProcess(FhirTerser terser, IBaseConformance stmt) {
		LOGGER.info("postProcess() invoked");
		super.postProcess(terser, stmt);
		ExtendedCapabilityStatement ecs = (ExtendedCapabilityStatement) stmt;
		ecs.setResourceSearchPaths(new ArrayList<>(this.searches));
		this.searches.clear();
	}

}
