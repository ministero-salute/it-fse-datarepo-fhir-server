package it.finanze.sanita.ms.serverfhir.custom.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Component
public class BasicAuthorizationInterceptor extends AuthorizationInterceptor {
	
	@Value("${basic-auth.admin-user}")
	private String adminUsr;
	
	@Value("${basic-auth.admin-password}")
	private String adminPwd;

	@Value("${basic-auth.human-user}")
	private String humanUsr;
	
	@Value("${basic-auth.human-password}")
	private String humanPwd;
	
	@Value("${basic-auth.status}")
	private Boolean status;
	
	@Override
	public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
		List<IAuthRule> out = null;
		Boolean allow = false;
		
		if(theRequestDetails.getCompleteUrl().contains("swagger-ui") || 
				theRequestDetails.getCompleteUrl().contains("api-docs") || 
				theRequestDetails.getCompleteUrl().endsWith("metadata")) {
			out = new RuleBuilder().allowAll().build();
		} else {
			if (status!=null && status) {
				BasicAuthDTO authInfo = BasicAuthUtility.calculate(theRequestDetails);
				allow = check(authInfo);
			} else {
				allow = true;
			}
			if (allow) {
				out = new RuleBuilder().allowAll().build();
			} else {
				// Throw an HTTP 401
				throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value");
			}
		}
		
		return out;
	}

	private boolean check(final BasicAuthDTO authInfo) {
		boolean output = false;
		try {
			boolean isAdmin = adminUsr.equals(authInfo.getUser()) && adminPwd.equals(authInfo.getPassword());
			boolean isHuman = humanUsr.equals(authInfo.getUser()) && humanPwd.equals(authInfo.getPassword());
			output = isAdmin || isHuman;
		} catch(Exception ex) {
			throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value");
		}
		return output;
	}
	
	
}
