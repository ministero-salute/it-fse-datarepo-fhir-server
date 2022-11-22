package it.finanze.sanita.ms.serverfhir.custom.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

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
		
		return out;
	}

	private boolean check(BasicAuthDTO authInfo) {
		boolean isAdmin = authInfo.getUser().equalsIgnoreCase(adminUsr) && authInfo.getPassword().equalsIgnoreCase(adminPwd);
		boolean isHuman = authInfo.getUser().equalsIgnoreCase(humanUsr) && authInfo.getPassword().equalsIgnoreCase(humanPwd);
		return isAdmin || isHuman;
	}

}