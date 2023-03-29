package it.finanze.sanita.ms.serverfhir.custom.capabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.StringType;

import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestfulServerConfiguration;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.util.FhirTerser;
import it.finanze.sanita.ms.serverfhir.custom.resource.ExtendedCapabilityStatement;
import it.finanze.sanita.ms.serverfhir.custom.resource.ExtendedCapabilityStatement.CustomCapabilityStatementRestResourceComponent;

public class JpaCapabilityStatementCustomProvider extends JpaCapabilityStatementProvider {

	
	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JpaCapabilityStatementCustomProvider.class);
	
	private ISearchParamRegistry searchParamRegistry;
	private RestfulServer rfServer;
	
	List<CustomCapabilityStatementRestResourceComponent> resourcesSearchPaths = new ArrayList<>();
	
	private RestfulServerConfiguration getServerConfiguration() {
		return rfServer.createConfiguration();
	}
	
	public JpaCapabilityStatementCustomProvider(RestfulServer theRestfulServer, IFhirSystemDao<?, ?> theSystemDao,
			DaoConfig theDaoConfig, ISearchParamRegistry theSearchParamRegistry,
			IValidationSupport theValidationSupport) {
		super(theRestfulServer, theSystemDao, theDaoConfig, theSearchParamRegistry, theValidationSupport);
		this.rfServer = theRestfulServer;
		this.searchParamRegistry = theSearchParamRegistry;
	}
	
	protected void setSearchParam(FhirTerser terser, IBase resource, String resourceName) {
		
		RestfulServerConfiguration serverConfiguration = getServerConfiguration();
		
		Map<String, RuntimeSearchParam> searchParams;
		if (this.searchParamRegistry != null) {
			searchParams = new HashMap<>(this.searchParamRegistry.getActiveSearchParams(resourceName));
			for (Entry<String, RuntimeSearchParam> nextBuiltInSp : serverConfiguration.getActiveSearchParams(resourceName).entrySet()) {
				String key = nextBuiltInSp.getKey();
				if (key.startsWith("_") && !searchParams.containsKey(key) && searchParamEnabled(key)) {
					searchParams.put(key, nextBuiltInSp.getValue());
				}
			}
		} else {
			searchParams = serverConfiguration.getActiveSearchParams(resourceName);
		}

		List<StringType> paths = searchParams.values()
			.stream()
			.filter(param -> param.getPath() != null)
			.map(param -> new StringType(param.getPath()))
			.collect(Collectors.toList());

		this.resourcesSearchPaths.add(new CustomCapabilityStatementRestResourceComponent(resourceName, paths));
		
	}
	
	
	@Override
	protected void postProcessRestResource(FhirTerser theTerser, IBase theResource, String theResourceName) {
		super.postProcessRestResource(theTerser, theResource, theResourceName);
		setSearchParam(theTerser, theResource, theResourceName);
	}
	
	@Override
	protected void postProcessRest(FhirTerser theTerser, IBase theRest) {
		super.postProcessRest(theTerser, theRest);
	}

	@Override
	protected void postProcess(FhirTerser theTerser, IBaseConformance theCapabilityStatement) {
		LOGGER.info("postProcess() invoked");
		super.postProcess(theTerser, theCapabilityStatement);
		ExtendedCapabilityStatement extendedCapabilityStatement = (ExtendedCapabilityStatement) theCapabilityStatement;
		extendedCapabilityStatement.setResourceSearchPaths(new ArrayList<>(this.resourcesSearchPaths) );
		this.resourcesSearchPaths.clear();
	}
	
}
