
package com.krishagni.catissueplus.core.biospecimen.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface SpecimenDao extends Dao<Specimen> {
	public List<Specimen> getSpecimens(SpecimenListCriteria crit);
	
	public Specimen getByLabel(String label);

	public Specimen getByBarcode(String barcode);
	
	public List<Specimen> getSpecimensByIds(List<Long> specimenIds);
	
	public List<Specimen> getSpecimensByVisitId(Long visitId);
	
	public List<Specimen> getSpecimensByVisitName(String visitName);
	
	public Specimen getSpecimenByVisitAndSr(Long visitId, Long srId);

	public Specimen getParentSpecimenByVisitAndSr(Long visitId, Long srId);

	public Map<String, Long> getCprAndVisitIds(Long specimenId);
	
	public Map<String, Set<Long>> getSpecimenSites(Set<Long> specimenIds);

	public List<Long> getDistributedSpecimens(List<Long> specimenIds);
}
