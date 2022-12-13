package it.finanze.sanita.ms.serverfhir.custom.audit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAgentComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventEntityComponent;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventOutcome;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventSourceComponent;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.api.server.ResponseDetails;
import it.finanze.sanita.ms.serverfhir.custom.auth.BasicAuthDTO;
import it.finanze.sanita.ms.serverfhir.custom.auth.BasicAuthUtility;
import it.finanze.sanita.ms.serverfhir.custom.crypt.CryptUtility;
import it.finanze.sanita.ms.serverfhir.custom.crypt.FPEFactory;
import it.finanze.sanita.ms.serverfhir.custom.helper.FHIRR4Helper;

@Component
@Interceptor
public class AuditInterceptor {

	@Autowired
	private FPEFactory fpeFactory;

	@Value("${audit.status}")
	private Boolean auditStatus;
	
	@Value("${basic-auth.admin-user}")
	private String adminUsr;
	
	@Value("${basic-auth.admin-password}")
	private String adminPwd;

	@Value("${crypt.status}")
	private Boolean cryptStatus;

	@Hook(value=Pointcut.SERVER_OUTGOING_RESPONSE)
	public boolean completed(RequestDetails theRequestDetails, ResponseDetails theResponseDetails) {

		if (cryptStatus!=null && cryptStatus) {
			CryptUtility.decrypt(theResponseDetails, fpeFactory.getAES());
		}
		
		if (auditStatus!=null && auditStatus) {
			BasicAuthDTO baDTO = BasicAuthUtility.calculate(theRequestDetails);
			AuditEventOutcome outcome = getOutcome(theResponseDetails.getResponseCode());
			AuditEvent auditEvent = createAuditEvent(isFromGateway(baDTO), baDTO.getUser(), theRequestDetails.getRequestType(), theRequestDetails.getResource(), outcome);
			if (auditEvent!=null) {
				System.out.println(FHIRR4Helper.serializeResource(auditEvent, true, false, false));
				//TODO: SAVE AUDIT EVENT
			}
			Bundle auditBundle = createAuditBundle(theRequestDetails.getResource());
			if (auditBundle!=null) {
				//TODO: SAVE BUNDLE AUDIT
			}
		}
		return true;
	}
	
	private Bundle createAuditBundle(IBaseResource resource) {
		Bundle out = null;
		if (resource instanceof Bundle) {
			Bundle bundle = (Bundle) resource;
			if (BundleType.TRANSACTION.equals(bundle.getType())) {
				out = bundle;
				out.setType(BundleType.DOCUMENT);
				for (BundleEntryComponent bec:out.getEntry()) {
					bec.setRequest(null);
				}
			}
		}
		return out;
	}

	private boolean isFromGateway(BasicAuthDTO baDTO) {
		boolean out = false;
		if (baDTO.getUser() != null && baDTO.getPassword() != null) {
			out = baDTO.getUser().equalsIgnoreCase(adminUsr) && baDTO.getPassword().equalsIgnoreCase(adminPwd);
		}
		return out;
	}

	private AuditEventOutcome getOutcome(int responseCode) {
		AuditEventOutcome out = AuditEventOutcome._12;
		if (responseCode>199 && responseCode<300) {
			out = AuditEventOutcome._0;
		} else if (responseCode>399 && responseCode<500) {
			out = AuditEventOutcome._4;
		} else if (responseCode>499 && responseCode<600) {
			out = AuditEventOutcome._8;
		}
		return out;
	}

	private AuditEvent createAuditEvent(boolean fromGateway, String inUser, RequestTypeEnum requestType, IBaseResource resource, AuditEventOutcome outcome) {
		AuditEvent ae = null;
		String user = "Anonymous";
		
		if (inUser != null && !inUser.isEmpty()) {
			user = inUser;
		}
		
		if (RequestTypeEnum.DELETE.equals(requestType) || RequestTypeEnum.PATCH.equals(requestType) || RequestTypeEnum.POST.equals(requestType) || RequestTypeEnum.PUT.equals(requestType)) {
			ae = new AuditEvent();
			AuditOperationEnum aoe = AuditOperationEnum.get(requestType, resource);
			Coding type = new Coding("http://terminology.hl7.org/CodeSystem/audit-event-type", "rest", "Restful Operation");
			ae.setType(type);
			List<Coding> subTypes = new ArrayList<Coding>();
			Coding subType = new Coding("http://hl7.org/fhir/restful-interaction", aoe.getCode(), aoe.getCode());
			subTypes.add(subType);
			ae.setSubtype(subTypes);
			ae.setAction(aoe.getAction());
			ae.setRecorded(new Date());
			ae.setOutcome(outcome);

			AuditEventSourceComponent source = new AuditEventSourceComponent();
			List<Coding> sourceTypes = new ArrayList<>();
			if (fromGateway) {
				source.setSite("Gateway");
				sourceTypes.add(new Coding("http://terminology.hl7.org/CodeSystem/security-source-type", "4", "Application Server"));
			} else {
				source.setSite("Other");
				sourceTypes.add(new Coding("http://terminology.hl7.org/CodeSystem/security-source-type", "1", "User Device"));
			}
			source.setType(sourceTypes);
			source.setObserver(new Reference("Ecosistema Dati Sanitari"));
			ae.setSource(source);
			
			List<AuditEventEntityComponent> entities = new ArrayList<>();

			AuditEventEntityComponent entity = new AuditEventEntityComponent();
			entity.setWhat(new Reference(resource.getIdElement()));//"Patient/example/_history/1"
			entity.setType(new Coding("http://terminology.hl7.org/CodeSystem/audit-entity-type", "2", "System Object"));
			entity.setLifecycle(new Coding("http://terminology.hl7.org/CodeSystem/dicom-audit-lifecycle", "6", "Access / Use"));
			entities.add(entity);
			ae.setEntity(entities);

			List<AuditEventAgentComponent> agents = new ArrayList<>();
			AuditEventAgentComponent agent = new AuditEventAgentComponent();
			
			Reference ref = new Reference();
			Identifier value = new Identifier();
			ref.setIdentifier(value);
			agent.setWho(ref);

			if (fromGateway) {
				agent.setType(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/extra-security-role-type", "authserver", "Authorization Server")));
				value.setValue("Gateway");
				agent.setAltId("Gateway");
				agent.setName("Gateway");
			} else {
				agent.setType(new CodeableConcept(new Coding("http://terminology.hl7.org/CodeSystem/extra-security-role-type", "humanuser", "Human User")));
				value.setValue(user);
				agent.setAltId(user);
				agent.setName(user);
			}
			agent.setRequestor(true);
			agents.add(agent);
			ae.setAgent(agents);
		}
		return ae;
	}

}
