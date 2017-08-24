
package com.krishagni.catissueplus.core.biospecimen.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.events.VisitSummary;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface VisitsDao extends Dao<Visit> {
	List<VisitSummary> getVisits(VisitsListCriteria crit);

	List<Visit> getVisitsList(VisitsListCriteria crit);
	
	Visit getByName(String name);
	
	List<Visit> getByName(Collection<String> names);

	List<Visit> getByIds(Collection<Long> ids);

	List<Visit> getBySpr(String sprNumber);

	Map<String, Object> getCprVisitIds(String key, Object value);

	Visit getLatestVisit(Long cprId);
}
