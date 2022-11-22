package it.finanze.sanita.ms.serverfhir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import it.finanze.sanita.ms.serverfhir.custom.auth.BasicAuthorizationInterceptor;

import javax.servlet.ServletException;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  @Autowired
  private BasicAuthorizationInterceptor bai;
  
  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    // Add your own customization here
    registerInterceptor(bai);
  }

}
