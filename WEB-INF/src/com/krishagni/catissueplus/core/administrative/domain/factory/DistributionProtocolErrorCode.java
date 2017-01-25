package com.krishagni.catissueplus.core.administrative.domain.factory;

import com.krishagni.commons.errors.ErrorCode;

public enum DistributionProtocolErrorCode implements ErrorCode {
	SHORT_TITLE_REQUIRED,

	DUP_SHORT_TITLE,

	TITLE_REQUIRED,
	
	INSTITUTE_REQUIRED,

	DUP_TITLE,

	NOT_FOUND,

	PI_REQUIRED,
	
	PI_NOT_FOUND,
	
	PI_DOES_NOT_BELONG_TO_INST,
	
	REF_ENTITY_FOUND,
	
	INVALID_DISTRIBUTING_INSTITUTE,
	
	INVALID_DISTRIBUTING_SITES,
	
	DISTRIBUTING_SITES_REQUIRED,
	
	PI_COORD_CANNOT_BE_SAME,

	EXPIRED,
	
	DUP_CONSENT,

	CONSENT_NOT_FOUND;

	@Override
	public String code() {
		return "DP_" + this.name();
	}
}
