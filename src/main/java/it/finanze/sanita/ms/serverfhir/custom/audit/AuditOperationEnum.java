package it.finanze.sanita.ms.serverfhir.custom.audit;

import org.hl7.fhir.dstu2.model.Bundle.BundleType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.AuditEvent.AuditEventAction;

import ca.uhn.fhir.rest.api.RequestTypeEnum;

public enum AuditOperationEnum {

	UPDATE("update", AuditEventAction.U),
	PATCH("patch", AuditEventAction.U),
	CREATE("create", AuditEventAction.C),
	DELETE("delete", AuditEventAction.D),
	TRANSACTION("transaction", AuditEventAction.E);

	private String code;
	private AuditEventAction action;
	
	AuditOperationEnum(String inCode, AuditEventAction inAction) {
		code = inCode;
		action = inAction;
	}
	
	public String getCode() {
		return code;
	}

	public AuditEventAction getAction() {
		return action;
	}

	static public AuditOperationEnum get(RequestTypeEnum requestType, IBaseResource resource) {
		AuditOperationEnum out = null;
		if (RequestTypeEnum.DELETE.equals(requestType)) {
			out = DELETE;
		} else if (RequestTypeEnum.POST.equals(requestType)) {
			out = CREATE;
			if (resource instanceof Bundle) {
				Bundle b = (Bundle) resource;
				if (BundleType.TRANSACTION.equals(b.getType())) {
					out = TRANSACTION;
				}
			}
		} else if (RequestTypeEnum.PATCH.equals(requestType)) {
			out = PATCH;
		} else if (RequestTypeEnum.PUT.equals(requestType)) {
			out = UPDATE;
		}
		return out;
	}
	
}
