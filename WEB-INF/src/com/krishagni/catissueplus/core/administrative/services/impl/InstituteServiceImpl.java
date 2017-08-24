package com.krishagni.catissueplus.core.administrative.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.krishagni.catissueplus.core.administrative.domain.Institute;
import com.krishagni.catissueplus.core.administrative.domain.factory.InstituteErrorCode;
import com.krishagni.catissueplus.core.administrative.domain.factory.InstituteFactory;
import com.krishagni.catissueplus.core.administrative.events.InstituteDetail;
import com.krishagni.catissueplus.core.administrative.events.InstituteQueryCriteria;
import com.krishagni.catissueplus.core.administrative.events.SiteSummary;
import com.krishagni.catissueplus.core.administrative.repository.InstituteListCriteria;
import com.krishagni.catissueplus.core.administrative.repository.SiteListCriteria;
import com.krishagni.catissueplus.core.administrative.services.InstituteService;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.access.AccessCtrlMgr;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.BulkDeleteEntityOp;
import com.krishagni.catissueplus.core.common.events.DependentEntityDetail;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;
import com.krishagni.catissueplus.core.exporter.domain.ExportJob;
import com.krishagni.catissueplus.core.exporter.services.ExportService;

public class InstituteServiceImpl implements InstituteService, InitializingBean {
	private DaoFactory daoFactory;

	private InstituteFactory instituteFactory;

	private ExportService exportSvc;
	
	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public void setInstituteFactory(InstituteFactory instituteFactory) {
		this.instituteFactory = instituteFactory;
	}

	public void setExportSvc(ExportService exportSvc) {
		this.exportSvc = exportSvc;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<InstituteDetail>> getInstitutes(RequestEvent<InstituteListCriteria> req) {
		try {
			InstituteListCriteria listCrit = req.getPayload();
			return ResponseEvent.response(daoFactory.getInstituteDao().getInstitutes(listCrit));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<Long> getInstitutesCount(RequestEvent<InstituteListCriteria> req) {
		try {
			InstituteListCriteria listCrit = req.getPayload();
			return ResponseEvent.response(daoFactory.getInstituteDao().getInstitutesCount(listCrit));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> getInstitute(RequestEvent<InstituteQueryCriteria> req) {
		try {
			InstituteQueryCriteria crit = req.getPayload();
			Institute institute = null;
			
			Object key = null;
			if (crit.getId() != null) {
				institute = daoFactory.getInstituteDao().getById(crit.getId());
				key = crit.getId();
			} else if (StringUtils.isNotBlank(crit.getName())) {
				institute = daoFactory.getInstituteDao().getInstituteByName(crit.getName());
				key = crit.getName();
			}
					
			if (institute == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND, key, 1);
			}
			
			return ResponseEvent.response(InstituteDetail.from(institute));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> createInstitute(RequestEvent<InstituteDetail> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			
			Institute institute = instituteFactory.createInstitute(req.getPayload());
			
			OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
			ensureUniqueName(institute.getName(), ose);
			ose.checkAndThrow();

			daoFactory.getInstituteDao().saveOrUpdate(institute, true);
			return ResponseEvent.response(InstituteDetail.from(institute));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<InstituteDetail> updateInstitute(RequestEvent<InstituteDetail> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			
			InstituteDetail detail = req.getPayload();			
			Institute existing = null;
			
			Object key = null;
			if (detail.getId() != null) {
				existing = daoFactory.getInstituteDao().getById(detail.getId());
				key = detail.getId();
			} else if (StringUtils.isNotBlank(detail.getName())) {
				existing = daoFactory.getInstituteDao().getInstituteByName(detail.getName());
				key = detail.getName();
			}
			
			if (existing == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND, key, 1);
			}
			
			Institute institute = instituteFactory.createInstitute(detail);
			if (!existing.getName().equals(institute.getName())) {
				OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
				ensureUniqueName(institute.getName(), ose);
				ose.checkAndThrow();
			}

			existing.update(institute);
			daoFactory.getInstituteDao().saveOrUpdate(existing);
			return ResponseEvent.response(InstituteDetail.from(existing));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	@PlusTransactional
	public ResponseEvent<List<DependentEntityDetail>> getDependentEntities(RequestEvent<Long> req) {
		try {
			Long instituteId = req.getPayload();
			Institute existing = daoFactory.getInstituteDao().getById(instituteId);
			if (existing == null) {
				return ResponseEvent.userError(InstituteErrorCode.NOT_FOUND, instituteId, 1);
			}
			
			return ResponseEvent.response(existing.getDependentEntities());
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}
	
	@Override
	@PlusTransactional
	public ResponseEvent<List<InstituteDetail>> deleteInstitutes(RequestEvent<BulkDeleteEntityOp> req) {
		try {
			AccessCtrlMgr.getInstance().ensureUserIsAdmin();
			
			Set<Long> instituteIds = req.getPayload().getIds();
			List<Institute> institutes = daoFactory.getInstituteDao().getByIds(instituteIds);
			if (instituteIds.size() != institutes.size()) {
				institutes.forEach(institute -> instituteIds.remove(institute.getId()));
				throw OpenSpecimenException.userError(InstituteErrorCode.NOT_FOUND, instituteIds, instituteIds.size());
			}

			List<InstituteDetail> deletedInstitutes = new ArrayList<>();
			for (Institute institute : institutes) {
				institute.delete(req.getPayload().isClose());
				deletedInstitutes.add(InstituteDetail.from(institute));
			}

			return ResponseEvent.response(deletedInstitutes);
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	//
	// Returns list of sites for a given institute without any authentication
	//
	@Override
	@PlusTransactional
	public ResponseEvent<List<SiteSummary>> getSites(RequestEvent<SiteListCriteria> req) {
		try {
			SiteListCriteria input = req.getPayload();
			if (StringUtils.isBlank(input.institute())) {
				return ResponseEvent.userError(InstituteErrorCode.NAME_REQUIRED);
			}

			SiteListCriteria curated = new SiteListCriteria().institute(input.institute()).query(input.query());
			return ResponseEvent.response(SiteSummary.from(daoFactory.getSiteDao().getSites(curated)));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		exportSvc.registerObjectsGenerator("institute", this::getInstitutesGenerator);
	}

	private void ensureUniqueName(String name, OpenSpecimenException ose) {
		Institute institute = daoFactory.getInstituteDao().getInstituteByName(name);
		if (institute != null) {
			ose.addError(InstituteErrorCode.DUP_NAME);
		}
	}

	private Function<ExportJob, List<? extends Object>> getInstitutesGenerator() {
		return new Function<ExportJob, List<? extends Object>>() {
			private boolean endOfInstitutes;

			private int startAt;

			@Override
			public List<? extends Object> apply(ExportJob job) {
				if (endOfInstitutes) {
					return Collections.emptyList();
				}

				InstituteListCriteria listCrit = new InstituteListCriteria();
				if (CollectionUtils.isNotEmpty(job.getRecordIds())) {
					listCrit.ids(job.getRecordIds());
					endOfInstitutes = true;
				} else {
					listCrit.startAt(startAt);
				}

				List<InstituteDetail> institutes = daoFactory.getInstituteDao().getInstitutes(listCrit);
				startAt += institutes.size();
				if (institutes.isEmpty()) {
					endOfInstitutes = true;
				}

				return institutes;
			}
		};
	}
}