package com.krishagni.catissueplus.core.common.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.krishagni.catissueplus.core.administrative.domain.Institute;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.BaseEntity;
import com.krishagni.catissueplus.core.common.domain.factory.LabelPrintRuleFactory;
import com.krishagni.catissueplus.core.common.util.Status;

public class PrintRuleConfig extends BaseEntity {
	private String objectType;

	private Institute institute;

	private User updatedBy;

	private Date updatedOn;

	private String activityStatus;

	private LabelPrintRule rule;

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Institute getInstitute() {
		return institute;
	}

	public void setInstitute(Institute institute) {
		this.institute = institute;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public LabelPrintRule getRule() {
		return rule;
	}

	public void setRule(LabelPrintRule rule) {
		this.rule = rule;
	}

	public String getRuleDefJson() {
		try {
			return getWriteMapper().writeValueAsString(getRuleDef());
		} catch (Exception e) {
			throw new RuntimeException("Error marshalling print rule to JSON", e);
		}
	}

	public void setRuleDefJson(String ruleDefJson) {
		Map<String, String> rule;
		try {
			rule = getReadMapper().readValue(ruleDefJson, new TypeReference<HashMap<String,Object>>() {});
		} catch (Exception e) {
			throw new RuntimeException("Error marshalling JSON to print rule", e);
		}

		setRuleDef(rule);
	}

	public Map<String, String> getRuleDef() {
		return getRule().toDefMap();
	}

	public void setRuleDef(Map<String, String> rule) {
		LabelPrintRuleFactory factory = LabelPrintRuleFactoryRegistrar.getInstance().getFactory(objectType);
		if (factory != null) {
			setRule(factory.createLabelPrintRule(rule));
		}
	}

	public void update(PrintRuleConfig rule) {
		setObjectType(rule.getObjectType());
		setInstitute(rule.getInstitute());
		setUpdatedBy(rule.getUpdatedBy());
		setUpdatedOn(rule.getUpdatedOn());
		setRule(rule.getRule());
		setActivityStatus(rule.getActivityStatus());
	}

	public void delete() {
		setActivityStatus(Status.ACTIVITY_STATUS_DISABLED.getStatus());
	}

	private ObjectMapper getReadMapper() {
		return new ObjectMapper();
	}

	private ObjectMapper getWriteMapper() {
		ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.setVisibilityChecker(
			mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(Visibility.ANY)
				.withGetterVisibility(Visibility.NONE)
				.withSetterVisibility(Visibility.NONE)
				.withCreatorVisibility(Visibility.NONE));
		return mapper;
	}

	private boolean isDisabled() {
		return Status.ACTIVITY_STATUS_DISABLED.getStatus().equals(getActivityStatus());
	}
}
