package it.finanze.sanita.ms.serverfhir.custom.capabilities;

import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.JpaCapabilityStatementProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.util.FhirTerser;
import it.finanze.sanita.ms.serverfhir.custom.resource.ExtendedCapabilityStatement;
import it.finanze.sanita.ms.serverfhir.custom.resource.ExtendedCapabilityStatement.CustomCapabilityStatementRestResourceComponent;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.r4.model.StringType;

import java.util.*;
import java.util.stream.Collectors;

public class JpaCapabilityStatementCustomProvider extends JpaCapabilityStatementProvider {


	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JpaCapabilityStatementCustomProvider.class);

	private final ISearchParamRegistry registry;

	private final List<CustomCapabilityStatementRestResourceComponent> searches;

	public JpaCapabilityStatementCustomProvider(RestfulServer theRestfulServer, IFhirSystemDao<?, ?> theSystemDao, DaoConfig theDaoConfig, ISearchParamRegistry theSearchParamRegistry, IValidationSupport theValidationSupport) {
		super(theRestfulServer, theSystemDao, theDaoConfig, theSearchParamRegistry, theValidationSupport);
		this.registry = theSearchParamRegistry;
		this.searches = new ArrayList<>();
	}

	protected void generateSearchParams(FhirTerser terser, IBase resource, String name) {

		String[] keywords = new String[]{"where", "as", "is"};

		Map<String, RuntimeSearchParam> params = new HashMap<>(registry.getActiveSearchParams(name));

		List<String> paths = params
			.values()
			.stream()
			.filter(p -> p.getBase().contains(name))
			.map(RuntimeSearchParam::getPath)
			.collect(Collectors.toList());

		List<String> pure = paths.stream().filter(s -> !s.contains("|")).collect(Collectors.toList());

		List<String> translated = paths.stream().filter(s -> s.contains("|")).map(s -> {
			List<String> condition = new ArrayList<>();
			String[] ops = s.split("\\|");
			for (String op : ops) {
				if(op.contains(name)) condition.add(op.trim());
			}
			return condition;
		}).flatMap(List::stream).filter(s -> !s.isEmpty()).collect(Collectors.toList());

		Set<String> out = new TreeSet<>();
		out.addAll(pure);
		out.addAll(translated);

		// Exclude query or casting
		out = out.stream().filter(s -> Arrays.stream(keywords).noneMatch(s::contains)).collect(Collectors.toSet());
		// Exclude wrong references due to similarity
		out = out.stream().filter(s -> s.split("\\.")[0].equals(name)).collect(Collectors.toSet());

		this.searches.add(new CustomCapabilityStatementRestResourceComponent(name, out.stream().map(StringType::new).collect(Collectors.toList())));
	}


	@Override
	protected void postProcessRestResource(FhirTerser terser, IBase res, String name) {
		super.postProcessRestResource(terser, res, name);
		generateSearchParams(terser, res, name);
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
