package com.krishagni.catissueplus.core.common.events;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.krishagni.catissueplus.core.common.domain.PrintRuleConfig;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PrintRuleConfigDetail {
	private Long id;

	private String objectType;

	private Long instituteId;

	private String instituteName;

	private UserSummary updatedBy;

	private Date updatedOn;

	private String activityStatus;

	private Map<String, String> rule;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Long getInstituteId() {
		return instituteId;
	}

	public void setInstituteId(Long instituteId) {
		this.instituteId = instituteId;
	}

	public String getInstituteName() {
		return instituteName;
	}

	public void setInstituteName(String instituteName) {
		this.instituteName = instituteName;
	}

	public UserSummary getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(UserSummary updatedBy) {
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

	public Map<String, String> getRule() {
		return rule;
	}

	public void setRule(Map<String, String> rule) {
		this.rule = rule;
	}

	public static PrintRuleConfigDetail from(PrintRuleConfig rule) {
		PrintRuleConfigDetail detail = new PrintRuleConfigDetail();

		detail.setId(rule.getId());
		detail.setObjectType(rule.getObjectType());
		detail.setInstituteId(rule.getInstitute() != null ? rule.getInstitute().getId() : null);
		detail.setInstituteName(rule.getInstitute() != null ? rule.getInstitute().getName() : null);
		detail.setUpdatedBy(UserSummary.from(rule.getUpdatedBy()));
		detail.setUpdatedOn(rule.getUpdatedOn());
		detail.setActivityStatus(rule.getActivityStatus());
		detail.setRule(rule.getRuleDef());
		return detail;
	}

	public static List<PrintRuleConfigDetail> from(Collection<PrintRuleConfig> rules) {
		return rules.stream().map(PrintRuleConfigDetail::from).collect(Collectors.toList());
	}
}