
package com.krishagni.catissueplus.core.biospecimen.repository.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;

import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;
import com.krishagni.catissueplus.core.biospecimen.domain.Visit;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenDao;
import com.krishagni.catissueplus.core.biospecimen.repository.SpecimenListCriteria;
import com.krishagni.catissueplus.core.common.repository.AbstractDao;

public class SpecimenDaoImpl extends AbstractDao<Specimen> implements SpecimenDao {
	public Class<?> getType() {
		return Specimen.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimens(SpecimenListCriteria crit) {
		Criteria query = getCurrentSession().createCriteria(Specimen.class, "specimen")
			.add(Subqueries.propertyIn("specimen.id", getSpecimenIdsQuery(crit)));

		if (crit.specimenListId() != null) {
			query.createAlias("specimen.specimenListItems", "listItem")
				.createAlias("listItem.list", "list")
				.add(Restrictions.eq("list.id", crit.specimenListId()))
				.addOrder(Order.asc("listItem.id"));
		} else {
			query.addOrder(Order.asc("specimen.id"));
		}

		if (crit.limitItems()) {
			query.setFirstResult(crit.startAt()).setMaxResults(crit.maxResults());
		}

		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Long> getSpecimenIds(SpecimenListCriteria crit) {
		return getSpecimenIdsQuery(crit).getExecutableCriteria(getCurrentSession()).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specimen getByLabel(String label) {
		List<Specimen> specimens = sessionFactory.getCurrentSession()
			.getNamedQuery(GET_BY_LABEL)
			.setString("label", label)
			.list();
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Specimen getByLabelAndCp(String cpShortTitle, String label) {
		List<Specimen> specimens = getCurrentSession().getNamedQuery(GET_BY_LABEL_AND_CP)
			.setString("label", label)
			.setString("cpShortTitle", cpShortTitle)
			.list();

		return specimens.isEmpty() ? null : specimens.iterator().next();
	}
	
	@Override
	public Specimen getSpecimenByVisitAndSr(Long visitId, Long srId) {
		return getByVisitAndSrId(GET_BY_VISIT_AND_SR, visitId, srId);
	}

	@Override
	public Specimen getParentSpecimenByVisitAndSr(Long visitId, Long srId) {
		return getByVisitAndSrId(GET_PARENT_BY_VISIT_AND_SR, visitId, srId);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Specimen getByBarcode(String barcode) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Specimen.class);
		query.add(Restrictions.eq("barcode", barcode));
		List<Specimen> specimens = query.list();
		
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByIds(List<Long> specimenIds) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_BY_IDS)
				.setParameterList("specimenIds", specimenIds)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByVisitId(Long visitId) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_BY_VISIT_ID)
				.setLong("visitId", visitId)
				.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Specimen> getSpecimensByVisitName(String visitName) {
		return sessionFactory.getCurrentSession()
				.getNamedQuery(GET_BY_VISIT_NAME)
				.setString("visitName", visitName)
				.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getCprAndVisitIds(String key, Object value) {
		List<Object[]> rows = getCurrentSession().createCriteria(Specimen.class)
			.createAlias("visit", "visit")
			.createAlias("visit.registration", "cpr")
			.createAlias("cpr.collectionProtocol", "cp")
			.setProjection(
				Projections.projectionList()
					.add(Projections.property("cp.id"))
					.add(Projections.property("cpr.id"))
					.add(Projections.property("visit.id"))
					.add(Projections.property("id")))
			.add(Restrictions.eq(key, value))
			.list();

		if (CollectionUtils.isEmpty(rows)) {
			return null;
		}
		
		Map<String, Object> result = new HashMap<>();
		Object[] row = rows.iterator().next();
		result.put("cpId",       row[0]);
		result.put("cprId",      row[1]);
		result.put("visitId",    row[2]);
		result.put("specimenId", row[3]);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, Set<Long>> getSpecimenSites(Set<Long> specimenIds) {
		Criteria query = getSessionFactory().getCurrentSession().createCriteria(Specimen.class)
				.createAlias("visit", "visit")
				.createAlias("visit.registration", "cpr")
				.createAlias("cpr.collectionProtocol", "cp")
				.createAlias("cp.sites", "cpSite")
				.createAlias("cpSite.site", "site");
		
		ProjectionList projs = Projections.projectionList();
		query.setProjection(projs);
		projs.add(Projections.property("id"));
		projs.add(Projections.property("site.id"));
		query.add(Restrictions.in("id", specimenIds));
		
		List<Object []> rows = query.list();
		Map<Long, Set<Long>> results = new HashMap<>();
		for (Object[] row: rows) {
			Long id = (Long)row[0];
			Long siteId = (Long)row[1];
			Set<Long> siteIds = results.get(id);
			if (siteIds == null) {
				siteIds = new HashSet<Long>();
				results.put(id, siteIds);
			}
			
			siteIds.add(siteId);
		}
		
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, String> getDistributionStatus(List<Long> specimenIds) {
		List<Object[]> rows = getSessionFactory().getCurrentSession()
			.getNamedQuery(GET_LATEST_DISTRIBUTION_AND_RETURN_DATES)
			.setParameterList("specimenIds", specimenIds)
			.list();

		return rows.stream().collect(
			Collectors.toMap(
				row -> (Long)row[0],
				row -> getDistributionStatus((Date)row[1], (Date)row[2])
			));
	}

	@Override
	public String getDistributionStatus(Long specimenId) {
		Map<Long, String> statuses = getDistributionStatus(Collections.singletonList(specimenId));
		return statuses.get(specimenId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Visit> getSpecimenVisits(SpecimenListCriteria crit) {
		boolean noLabels = CollectionUtils.isEmpty(crit.labels());
		boolean noIds = CollectionUtils.isEmpty(crit.ids());

		if (noLabels && noIds && crit.specimenListId() == null) {
			throw new IllegalArgumentException("No limiting condition on specimens");
		}

		Criteria query = getSessionFactory().getCurrentSession()
			.createCriteria(Visit.class, "visit")
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
			.createAlias("visit.specimens", "specimen");

		if (!noIds) {
			addIdsCond(query, crit.ids());
		} else if (!noLabels) {
			addLabelsCond(query, crit.labels());
		}

		addSiteCpsCond(query, crit);
		addSpecimenListCond(query, crit);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean areDuplicateLabelsPresent() {
		List<Object[]> rows = getSessionFactory().getCurrentSession()
			.getNamedQuery(GET_DUPLICATE_LABEL_COUNT)
			.list();

		return rows.size() > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, Long> getSpecimenStorageSite(Set<Long> specimenIds) {
		List<Object[]> rows = getCurrentSession().getNamedQuery(GET_STORAGE_SITE)
			.setParameterList("specimenIds", specimenIds)
			.list();

		// null value for site means virtual specimen
		HashMap<Long, Long> result = new HashMap<>();
		rows.forEach((row) -> result.put((Long)row[0], (Long)row[1]));
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getNonCompliantSpecimens(SpecimenListCriteria crit) {
		if (CollectionUtils.isEmpty(crit.ids()) && crit.specimenListId() == null) {
			return Collections.emptyList();
		}

		Criteria query = getCurrentSession().createCriteria(Specimen.class, "specimen")
			.setProjection(Projections.property("specimen.label"))
			.add(Subqueries.propertyNotIn("specimen.id", getSpecimenIdsQuery(crit)));

		if (CollectionUtils.isNotEmpty(crit.ids())) {
			addIdsCond(query, crit.ids());
		}

		addSpecimenListCond(query, crit);
		return query.list();
	}

	private void addIdsCond(Criteria query, List<Long> ids) {
		addInCond(query, "specimen.id", ids);
	}

	private void addLabelCond(Junction condition, String label, MatchMode matchMode) {
		if (matchMode == MatchMode.EXACT) {
			condition.add(Restrictions.eq("specimen.label", label));
		} else {
			condition.add(Restrictions.ilike("specimen.label", label, matchMode));
		}
	}

	private void addLabelsCond(Disjunction condition, List<String> labels) {
		addInCond(condition, "specimen.label", labels);
	}

	private void addLabelsCond(Criteria query, List<String> labels) {
		addInCond(query, "specimen.label", labels);
	}

	private void addBarcodeCond(Junction condition, String barcode, MatchMode matchMode) {
		if (matchMode == MatchMode.EXACT) {
			condition.add(Restrictions.eq("specimen.barcode", barcode));
		} else {
			condition.add(Restrictions.ilike("specimen.barcode", barcode, matchMode));
		}
	}

	private void addBarcodesCond(Disjunction condition, List<String> barcodes) {
		addInCond(condition, "specimen.barcode", barcodes);
	}

	private <T> void addInCond(Disjunction condition, String property, List<T> values) {
		int numValues = values.size();
		for (int i = 0; i < numValues; i += 500) {
			List<T> params = values.subList(i, i + 500 > numValues ? numValues : i + 500);
			condition.add(Restrictions.in(property, params));
		}
	}

	private <T> void addInCond(Criteria query, String property, List<T> values) {
		Disjunction labelIn = Restrictions.disjunction();
		addInCond(labelIn, property, values);
		query.add(labelIn);
	}

	private DetachedCriteria getSpecimenIdsQuery(SpecimenListCriteria crit) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Specimen.class, "specimen")
			.setProjection(Projections.distinct(Projections.property("specimen.id")));
		Criteria query = detachedCriteria.getExecutableCriteria(getCurrentSession());

		if (CollectionUtils.isNotEmpty(crit.ids())) {
			addIdsCond(query, crit.ids());
		} else {
			Disjunction labelOrBarcode = Restrictions.disjunction();
			if (CollectionUtils.isNotEmpty(crit.labels())) {
				if (crit.labels().size() == 1) {
					addLabelCond(labelOrBarcode, crit.labels().iterator().next(), crit.matchMode());
				} else {
					addLabelsCond(labelOrBarcode, crit.labels());
				}
			}

			if (CollectionUtils.isNotEmpty(crit.barcodes())) {
				if (crit.barcodes().size() == 1) {
					addBarcodeCond(labelOrBarcode, crit.barcodes().iterator().next(), crit.matchMode());
				} else {
					addBarcodesCond(labelOrBarcode, crit.barcodes());
				}
			}

			query.add(labelOrBarcode);
		}

		addLineageCond(query, crit);
		addCollectionStatusCond(query, crit);
		addSiteCpsCond(query, crit);
		addCpCond(query, crit);
		addPpidCond(query, crit);
		addSpecimenListCond(query, crit);
		addStorageLocationCond(query, crit);
		addSpecimenTypeCond(query, crit);
		addAnatomicSiteCond(query, crit);
		addAvailableSpecimenCond(query, crit);
		addNoQtySpecimenCond(query, crit);
		return detachedCriteria;
	}

	private void addLineageCond(Criteria query, SpecimenListCriteria crit) {
		if (crit.lineages() == null || crit.lineages().length == 0) {
			return;
		}

		query.add(Restrictions.in("lineage", crit.lineages()));
	}

	private void addCollectionStatusCond(Criteria query, SpecimenListCriteria crit) {
		if (crit.collectionStatuses() == null || crit.collectionStatuses().length == 0) {
			return;
		}

		query.add(Restrictions.in("collectionStatus", crit.collectionStatuses()));
	}

	private void addSiteCpsCond(Criteria query, SpecimenListCriteria crit) {
		BiospecimenDaoHelper.getInstance().addSiteCpsCond(query, crit);
	}

	private void addCpCond(Criteria query, SpecimenListCriteria crit) {
		if (crit.cpId() == null) {
			return;
		}

		if (CollectionUtils.isEmpty(crit.siteCps())) {
			if (!query.getAlias().equals("visit")) {
				query.createAlias("specimen.visit", "visit");
			}

			query.createAlias("visit.registration", "cpr")
				.createAlias("cpr.collectionProtocol", "cp");
		}

		query.add(Restrictions.eq("cp.id", crit.cpId()));
	}

	private void addPpidCond(Criteria query, SpecimenListCriteria crit) {
		if (StringUtils.isBlank(crit.ppid())) {
			return;
		}

		if (CollectionUtils.isEmpty(crit.siteCps()) && crit.cpId() == null) {
			if (!query.getAlias().equals("visit")) {
				query.createAlias("specimen.visit", "visit");
			}

			query.createAlias("visit.registration", "cpr");
		}

		query.add(Restrictions.ilike("cpr.ppid", crit.ppid(), crit.matchMode()));
	}

	private void addSpecimenListCond(Criteria query, SpecimenListCriteria crit) {
		if (crit.specimenListId() == null) {
			return;
		}

		query.createAlias("specimen.specimenListItems", "listItem")
			.createAlias("listItem.list", "list")
			.add(Restrictions.eq("list.id", crit.specimenListId()));
	}

	private void addStorageLocationCond(Criteria query, SpecimenListCriteria crit) {
		if (StringUtils.isBlank(crit.storageLocationSite()) &&
			StringUtils.isBlank(crit.container()) &&
			crit.containerId() == null &&
			crit.ancestorContainerId() == null) {
			return;
		}

		query.createAlias("specimen.position", "pos", JoinType.LEFT_OUTER_JOIN)
			.createAlias("pos.container", "cont", JoinType.LEFT_OUTER_JOIN);

		if (crit.ancestorContainerId() != null) {
			query.createAlias("cont.ancestorContainers", "ancestor")
					.add(Restrictions.eq("ancestor.id", crit.ancestorContainerId()));
		}

		if (StringUtils.isNotBlank(crit.storageLocationSite())) {
			query.createAlias("cont.site", "contSite", JoinType.LEFT_OUTER_JOIN)
				.add(Restrictions.or(
					Restrictions.isNull("pos.id"),
					Restrictions.eq("contSite.name", crit.storageLocationSite())
				));
		}

		if (StringUtils.isNotBlank(crit.container())) {
			query.add(Restrictions.eq("cont.name", crit.container()));
		} else if (crit.containerId() != null) {
			query.add(Restrictions.eq("cont.id", crit.containerId()));
		}
	}

	@SuppressWarnings("unchecked")
	private Specimen getByVisitAndSrId(String hql, Long visitId, Long srId) {
		List<Specimen> specimens = sessionFactory.getCurrentSession()
				.getNamedQuery(hql)
				.setLong("visitId", visitId)
				.setLong("srId", srId)
				.list();
		return specimens.isEmpty() ? null : specimens.iterator().next();
	}

	private String getDistributionStatus(Date execDate, Date returnDate) {
		return (returnDate == null || execDate.after(returnDate)) ? "Distributed" : "Returned";
	}

	private void addSpecimenTypeCond(Criteria query, SpecimenListCriteria crit) {
		if (StringUtils.isBlank(crit.type())) {
			return;
		}

		query.add(Restrictions.eq("specimenType", crit.type()));
	}

	private void addAnatomicSiteCond(Criteria query, SpecimenListCriteria crit) {
		if (StringUtils.isBlank(crit.anatomicSite())) {
			return;
		}

		query.add(Restrictions.eq("tissueSite", crit.anatomicSite()));
	}

	private void addAvailableSpecimenCond(Criteria query, SpecimenListCriteria crit) {
		if (!crit.available()) {
			return;
		}

		query.add(
			Restrictions.disjunction()
				.add(Restrictions.isNull("availableQuantity"))
				.add(Restrictions.gt("availableQuantity", new BigDecimal(0)))
		);
	}

	private void addNoQtySpecimenCond(Criteria query, SpecimenListCriteria crit) {
		if (!crit.noQty()) {
			return;
		}

		query.add(
			Restrictions.disjunction()
				.add(Restrictions.isNull("availableQuantity"))
				.add(Restrictions.eq("availableQuantity", BigDecimal.ZERO))
		);
	}

	private static final String FQN = Specimen.class.getName();
	
	private static final String GET_BY_LABEL = FQN + ".getByLabel";

	private static final String GET_BY_LABEL_AND_CP = FQN + ".getByLabelAndCp";
	
	private static final String GET_BY_VISIT_AND_SR = FQN + ".getByVisitAndReq";

	private static final String GET_PARENT_BY_VISIT_AND_SR = FQN + ".getParentByVisitAndReq";
	
	private static final String GET_BY_IDS = FQN + ".getByIds";
	
	private static final String GET_BY_VISIT_ID = FQN + ".getByVisitId";
	
	private static final String GET_BY_VISIT_NAME = FQN + ".getByVisitName";
	
	private static final String GET_LATEST_DISTRIBUTION_AND_RETURN_DATES = FQN + ".getLatestDistributionAndReturnDates";

	private static final String GET_DUPLICATE_LABEL_COUNT = FQN + ".getDuplicateLabelCount";

	private static final String GET_STORAGE_SITE = FQN + ".getStorageSite";
}
