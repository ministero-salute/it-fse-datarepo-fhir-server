package it.finanze.sanita.ms.serverfhir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.rest.openapi.OpenApiInterceptor;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration 
public class OpenApiCFG extends OpenApiInterceptor {
 
	final String securitySchemeName = "basic";
	
	@Value("${basic-auth.status}")
	private Boolean status;

	@Override
	protected OpenAPI generateOpenApi(ServletRequestDetails theRequestDetails) {
		OpenAPI op = super.generateOpenApi(theRequestDetails);
		if (status!=null && status) {
			op.addSecurityItem(new SecurityRequirement()
					.addList(securitySchemeName)).getComponents().addSecuritySchemes(securitySchemeName, 
					new SecurityScheme().name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("basic"));
		}
		return op;
	}
	
}
