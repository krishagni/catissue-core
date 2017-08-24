
package com.krishagni.catissueplus.core.biospecimen.repository;

import java.util.List;
import java.util.Map;

import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocolRegistration;
import com.krishagni.catissueplus.core.biospecimen.events.CprSummary;
import com.krishagni.catissueplus.core.common.repository.Dao;

public interface CollectionProtocolRegistrationDao extends Dao<CollectionProtocolRegistration> {	
	List<CprSummary> getCprList(CprListCriteria crit);

	Long getCprCount(CprListCriteria crit);

	List<CollectionProtocolRegistration> getCprs(CprListCriteria crit);

	List<CollectionProtocolRegistration> getCprsByCpId(Long cpId, int startAt, int maxResults);

	CollectionProtocolRegistration getCprByPpid(Long cpId, String ppid);
	
	CollectionProtocolRegistration getCprByPpid(String cpTitle, String ppid);
	
	CollectionProtocolRegistration getCprByCpShortTitleAndPpid(String cpShortTitle, String ppid);

	CollectionProtocolRegistration getCprByCpShortTitleAndEmpi(String cpShortTitle, String empi);

	CollectionProtocolRegistration getCprByBarcode(String barcode);

	CollectionProtocolRegistration getCprByParticipantId(Long cpId, Long participantId);

	Map<String, Object> getCprIds(String key, Object value);


}
