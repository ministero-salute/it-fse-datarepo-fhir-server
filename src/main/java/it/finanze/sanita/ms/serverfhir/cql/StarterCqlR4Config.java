package it.finanze.sanita.ms.serverfhir.cql;

import ca.uhn.fhir.cql.config.CqlR4Config;
import it.finanze.sanita.ms.serverfhir.annotations.OnR4Condition;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@Conditional({OnR4Condition.class, CqlConfigCondition.class})
@Import({CqlR4Config.class})
public class StarterCqlR4Config {
}
