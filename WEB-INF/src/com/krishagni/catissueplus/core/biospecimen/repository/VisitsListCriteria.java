package com.krishagni.catissueplus.core.biospecimen.repository;

import java.util.List;

import com.krishagni.catissueplus.core.common.Pair;
import com.krishagni.catissueplus.core.common.events.AbstractListCriteria;

public class VisitsListCriteria extends AbstractListCriteria<VisitsListCriteria> {
	private Long cprId;

	private String name;

	private String sprNumber;

	private Long cpId;

	private List<String> names;

	private List<Pair<Long, Long>> siteCps;

	private boolean useMrnSites;

	@Override
	public VisitsListCriteria self() {
		return this;
	}
	
	
	public Long cprId() {
		return this.cprId;
	}
	
	public VisitsListCriteria cprId(Long cprId) {
		this.cprId = cprId;
		return self();
	}

	public String name() {
		return this.name;
	}

	public VisitsListCriteria name(String name) {
		this.name = name;
		return self();
	}

	public String sprNumber() {
		return this.sprNumber;
	}

	public VisitsListCriteria sprNumber(String sprNumber) {
		this.sprNumber = sprNumber;
		return self();
	}

	public Long cpId() {
		return cpId;
	}

	public VisitsListCriteria cpId(Long cpId) {
		this.cpId = cpId;
		return self();
	}

	public List<String> names() {
		return names;
	}

	public VisitsListCriteria names(List<String> names) {
		this.names = names;
		return self();
	}

	public List<Pair<Long, Long>> siteCps() {
		return siteCps;
	}

	public VisitsListCriteria siteCps(List<Pair<Long, Long>> siteCps) {
		this.siteCps = siteCps;
		return self();
	}

	public boolean useMrnSites() {
		return useMrnSites;
	}

	public VisitsListCriteria useMrnSites(boolean useMrnSites) {
		this.useMrnSites = useMrnSites;
		return self();
	}
}
