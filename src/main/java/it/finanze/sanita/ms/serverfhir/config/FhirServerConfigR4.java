package it.finanze.sanita.ms.serverfhir.config;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.jpa.config.BaseJavaConfigR4;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import it.finanze.sanita.ms.serverfhir.AppProperties;
import it.finanze.sanita.ms.serverfhir.EnvironmentHelper;
import it.finanze.sanita.ms.serverfhir.annotations.OnR4Condition;
import it.finanze.sanita.ms.serverfhir.cql.StarterCqlR4Config;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
@Conditional(OnR4Condition.class)
@Import({StarterCqlR4Config.class, ElasticsearchConfig.class})
public class FhirServerConfigR4 extends BaseJavaConfigR4 {

  @Autowired
  private DataSource myDataSource;

  /**
   * We override the paging provider definition so that we can customize
   * the default/max page sizes for search results. You can set these however
   * you want, although very large page sizes will require a lot of RAM.
   */
  @Autowired
  AppProperties appProperties;

  @PostConstruct
  public void initSettings() {
    if(appProperties.getSearch_coord_core_pool_size() != null) {
		 setSearchCoordCorePoolSize(appProperties.getSearch_coord_core_pool_size());
	 }
	  if(appProperties.getSearch_coord_max_pool_size() != null) {
		  setSearchCoordMaxPoolSize(appProperties.getSearch_coord_max_pool_size());
	  }
	  if(appProperties.getSearch_coord_queue_capacity() != null) {
		  setSearchCoordQueueCapacity(appProperties.getSearch_coord_queue_capacity());
	  }
  }

  @Override
  public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
    DatabaseBackedPagingProvider pagingProvider = super.databaseBackedPagingProvider();
    pagingProvider.setDefaultPageSize(appProperties.getDefault_page_size());
    pagingProvider.setMaximumPageSize(appProperties.getMax_page_size());
    return pagingProvider;
  }

  @Autowired
  private ConfigurableEnvironment configurableEnvironment;

  @Override
  @Bean()
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
	  ConfigurableListableBeanFactory myConfigurableListableBeanFactory) {
    LocalContainerEntityManagerFactoryBean retVal = super.entityManagerFactory(myConfigurableListableBeanFactory);
    retVal.setPersistenceUnitName("HAPI_PU");

    try {
      retVal.setDataSource(myDataSource);
    } catch (Exception e) {
      throw new ConfigurationException("Could not set the data source due to a configuration issue", e);
    }

    retVal.setJpaProperties(EnvironmentHelper.getHibernateProperties(configurableEnvironment,
		 myConfigurableListableBeanFactory));
    return retVal;
  }

  @Bean
  @Primary
  public JpaTransactionManager hapiTransactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager retVal = new JpaTransactionManager();
    retVal.setEntityManagerFactory(entityManagerFactory);
    return retVal;
  }

}
